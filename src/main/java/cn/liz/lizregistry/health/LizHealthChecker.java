package cn.liz.lizregistry.health;

import cn.liz.lizregistry.model.InstanceMeta;
import cn.liz.lizregistry.service.LizRegistryService;
import cn.liz.lizregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LizHealthChecker implements HealthChecker {

    RegistryService registryService;

    public LizHealthChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    long timeout = 20_000;

    @Override
    public void start() {
        executorService.scheduleWithFixedDelay(
                () -> {
                    log.info("======> health checker start...");
                    long now = System.currentTimeMillis();
                    LizRegistryService.TIMESTAMPS.keySet().stream().forEach(serviceAndInstance -> {
                        long timestamp = LizRegistryService.TIMESTAMPS.get(serviceAndInstance);
                        if (now - timestamp > timeout) {
                            log.info("======> health checker :{} is down", serviceAndInstance);
                            int index = serviceAndInstance.indexOf("@");
                            String service = serviceAndInstance.substring(0, index);
                            String url = serviceAndInstance.substring(index + 1);
                            InstanceMeta instance = InstanceMeta.from(url);
                            registryService.unregister(service, instance);
                            LizRegistryService.TIMESTAMPS.remove(serviceAndInstance);
                        }
                    });
                }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }
}
