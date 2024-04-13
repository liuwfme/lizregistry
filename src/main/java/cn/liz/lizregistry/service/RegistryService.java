package cn.liz.lizregistry.service;

import cn.liz.lizregistry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

public interface RegistryService {

    InstanceMeta register(String serviceName, InstanceMeta instanceMeta);

    InstanceMeta unregister(String serviceName, InstanceMeta instanceMeta);

    List<InstanceMeta> getAllInstances(String serviceName);

    long renew(InstanceMeta instance, String... services);

    Long version(String service);

    Map<String, Long> versions(String... services);
}
