package cn.liz.lizregistry.config;

import cn.liz.lizregistry.health.HealthChecker;
import cn.liz.lizregistry.health.LizHealthChecker;
import cn.liz.lizregistry.service.LizRegistryService;
import cn.liz.lizregistry.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LizRegistryConfig {
    @Bean
    RegistryService registryService() {
        return new LizRegistryService();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public HealthChecker healthChecker(@Autowired RegistryService registryService) {
        return new LizHealthChecker(registryService);
    }
}
