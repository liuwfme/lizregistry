package cn.liz.lizregistry.service;

import cn.liz.lizregistry.cluster.Snapshot;
import cn.liz.lizregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
public class LizRegistryService implements RegistryService {

    /**
     * k：服务名；v：服务的实例集合
     */
    final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();

    /**
     * k：服务名；v：服务版本号
     */
    final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();

    /**
     * k：服务名+实例url；v：实例最后一次探活的时间戳
     */
    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();

    public final static AtomicLong VERSION = new AtomicLong(0);

    @Override
    public synchronized InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> instanceMetas = REGISTRY.get(service);
        if (instanceMetas != null && !instanceMetas.isEmpty()) {
            if (instanceMetas.contains(instance)) {
                log.info("======>> already registered instance:{}", instance.toUrl());
                instance.setStatus(true);
                return instance;
            }
        }

        log.info("======>> register instance:{}", instance.toUrl());
        REGISTRY.add(service, instance);
        instance.setStatus(true);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public synchronized InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if (metas == null || metas.isEmpty()) {
            return null;
        }
        log.info("======>> unregister instance:{}", instance.toUrl());
        metas.removeIf(meta -> meta.equals(instance));
        instance.setStatus(false);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String serviceName) {
        return REGISTRY.get(serviceName);
    }

    @Override
    public synchronized long renew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service + "@" + instance.toUrl(), now);
        }
        return now;
    }

    @Override
    public Long version(String service) {
        return VERSIONS.get(service);
    }

    @Override
    public Map<String, Long> versions(String... services) {
        return Arrays.stream(services).collect(Collectors.toMap(k -> k, VERSIONS::get, (v1, v2) -> v2));
    }

    public static synchronized Snapshot snapshot() {
        LinkedMultiValueMap<String, InstanceMeta> registry = new LinkedMultiValueMap<>();
        registry.addAll(REGISTRY);
        Map<String, Long> versions = new HashMap<>(VERSIONS);
        Map<String, Long> timestamps = new HashMap<>(TIMESTAMPS);
        return new Snapshot(registry, versions, timestamps, VERSION.get());
    }

    public static synchronized long restore(Snapshot snapshot) {
        REGISTRY.clear();
        REGISTRY.addAll(snapshot.getREGISTRY());
        VERSIONS.clear();
        VERSIONS.putAll(snapshot.getVERSIONS());
        TIMESTAMPS.clear();
        TIMESTAMPS.putAll(snapshot.getTIMESTAMPS());
        VERSION.set(snapshot.getVersion());
        return snapshot.getVersion();
    }
}
