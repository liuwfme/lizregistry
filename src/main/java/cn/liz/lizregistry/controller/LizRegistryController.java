package cn.liz.lizregistry.controller;

import cn.liz.lizregistry.cluster.Cluster;
import cn.liz.lizregistry.cluster.Server;
import cn.liz.lizregistry.cluster.Snapshot;
import cn.liz.lizregistry.model.InstanceMeta;
import cn.liz.lizregistry.service.LizRegistryService;
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

    @Autowired
    Cluster cluster;

    @RequestMapping("/register")
    public void register(String serviceName, @RequestBody InstanceMeta instanceMeta) {
        checkLeader();
        log.info("======>> register service:{}, instance:{}", serviceName, instanceMeta);
        registryService.register(serviceName, instanceMeta);
    }

    private void checkLeader() {
        if (!cluster.self().isLeader()) {
            throw new RuntimeException("current server is not a leader, leader:" + cluster.leader().getUrl());
        }
    }

    @RequestMapping("/unregister")
    public void unregister(String serviceName, @RequestBody InstanceMeta instanceMeta) {
        checkLeader();
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
        checkLeader();
        log.info("======>> renew service:{}, instance:{}", serviceName, instanceMeta);
        return registryService.renew(instanceMeta, serviceName);
    }

    @RequestMapping("/renews")
    public long renews(String serviceNames, @RequestBody InstanceMeta instanceMeta) {
        checkLeader();
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

    @RequestMapping("/info")
    public Server info() {
        Server server = cluster.self();
        log.info("======>> info:{}", server);
        return server;
    }

    @RequestMapping("/cluster")
    public List<Server> cluster() {
        List<Server> servers = cluster.getServers();
        log.info("======>> cluster:{}", servers);
        return servers;
    }

    @RequestMapping("/leader")
    public Server leader() {
        Server server = cluster.leader();
        log.info("======>> leader:{}", server);
        return server;
    }

    @RequestMapping("/setLeader")
    public Server setLeader() {
        Server server = cluster.self();
        server.setLeader(true);
        log.info("======>> setLeader:{}", server);
        return server;
    }

    @RequestMapping("/snapshot")
    public Snapshot snapshot() {
        Snapshot snapshot = LizRegistryService.snapshot();
        log.info("======>> snapshot:{}", snapshot);
        return snapshot;
    }

}
