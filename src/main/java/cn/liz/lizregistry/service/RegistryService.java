package cn.liz.lizregistry.service;

import cn.liz.lizregistry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

public interface RegistryService {

    /**
     * 注册服务的实例
     */
    InstanceMeta register(String serviceName, InstanceMeta instanceMeta);

    /**
     * 反注册服务的实例
     */
    InstanceMeta unregister(String serviceName, InstanceMeta instanceMeta);

    /**
     * 获取服务的所有实例
     */
    List<InstanceMeta> getAllInstances(String serviceName);

    /**
     * 保活
     */
    long renew(InstanceMeta instance, String... services);

    /**
     * 获取服务版本号
     */
    Long version(String service);

    /**
     * 获取服务版本号
     */
    Map<String, Long> versions(String... services);
}
