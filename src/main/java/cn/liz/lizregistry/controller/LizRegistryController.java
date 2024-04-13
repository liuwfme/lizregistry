package cn.liz.lizregistry.controller;

import cn.liz.lizregistry.model.InstanceMeta;
import cn.liz.lizregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class LizRegistryController {

    @Autowired
    RegistryService registryService;


    @RequestMapping("/register")
    public void register(String serviceName, @RequestBody InstanceMeta instanceMeta) {
        log.info("======>> register service:{}, instance:{}", serviceName, instanceMeta);
        registryService.register(serviceName, instanceMeta);
    }

    @RequestMapping("/unregister")
    public void unregister(String serviceName, @RequestBody InstanceMeta instanceMeta) {
        log.info("======>> unregister service:{}, instance:{}", serviceName, instanceMeta);
        registryService.unregister(serviceName, instanceMeta);
    }

    @RequestMapping("/findAll")
    public List<InstanceMeta> findAllInstances(String serviceName) {
        log.info("======>> findAllInstances service:{}", serviceName);
        return registryService.getAllInstances(serviceName);
    }

    @RequestMapping("/renew")
    public long renew(String serviceName, @RequestBody InstanceMeta instanceMeta) {
        log.info("======>> renew service:{}, instance:{}", serviceName, instanceMeta);
        return registryService.renew(instanceMeta, serviceName);
    }

    @RequestMapping("/renews")
    public long renews(String serviceNames, @RequestBody InstanceMeta instanceMeta) {
        log.info("======>> renews services:{}, instance:{}", serviceNames, instanceMeta);
        return registryService.renew(instanceMeta, serviceNames.split(","));
    }

    @RequestMapping("/version")
    public long version(@RequestParam String service) {
        log.info("======>> version service:{}", service);
        return registryService.version(service);
    }

    @RequestMapping("/versions")
    public Map<String, Long> versions(@RequestParam String services) {
        log.info("======>> versions services:{}", services);
        return registryService.versions(services);
    }

}
