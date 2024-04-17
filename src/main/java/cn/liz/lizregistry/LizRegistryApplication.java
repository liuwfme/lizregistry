package cn.liz.lizregistry;

import cn.liz.lizregistry.config.LizRegistryConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {LizRegistryConfigProperties.class})
public class LizRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LizRegistryApplication.class, args);
    }

}
