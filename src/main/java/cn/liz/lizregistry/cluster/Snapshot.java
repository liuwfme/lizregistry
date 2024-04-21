package cn.liz.lizregistry.cluster;

import cn.liz.lizregistry.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Snapshot {
    // k：服务名；v：实例
    LinkedMultiValueMap<String, InstanceMeta> REGISTRY;

    // k：服务名；v：版本号
    Map<String, Long> VERSIONS;

    // k：服务名+实例url；v：实例最后一次探活的时间戳
    Map<String, Long> TIMESTAMPS;
    long version;
}
