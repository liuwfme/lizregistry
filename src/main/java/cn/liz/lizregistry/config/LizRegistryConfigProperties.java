package cn.liz.lizregistry.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "lizregistry")
public class LizRegistryConfigProperties {
    private List<String> serverList;
}
