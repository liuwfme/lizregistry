package cn.liz.lizregistry.cluster;

import cn.liz.lizregistry.config.LizRegistryConfigProperties;
import cn.liz.lizregistry.service.LizRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Cluster {

    @Value("${server.port}")
    String port;

    String host;

    Server MYSELF;

    LizRegistryConfigProperties registryConfigProperties;

    public Cluster(LizRegistryConfigProperties properties) {
        this.registryConfigProperties = properties;
    }

    private List<Server> servers;

    public void init() {
        host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
        log.info("====== findFirstNonLoopbackHostInfo :{}", host);

        MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);
        log.info("====== myself:{}", MYSELF);

        initServers();

        new ServerHealth(this).checkServerHealth();
    }

    private void initServers() {
        List<Server> servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerList()) {
            Server server = new Server();
            if (url.contains("localhost")) {
                url = url.replace("localhost", host);
            } else if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", host);
            }
            if (url.equals(MYSELF.getUrl())) {
                servers.add(MYSELF);
            } else {
                server.setUrl(url);
                server.setStatus(false);
                server.setLeader(false);
                server.setVersion(-1L);
                servers.add(server);
            }
        }
        this.servers = servers;
    }

    public Server self() {
        MYSELF.setVersion(LizRegistryService.VERSION.get());
        return MYSELF;
    }

    public List<Server> getServers() {
        return servers;
    }

    public Server leader() {
        return this.servers.stream().filter(Server::isStatus).filter(Server::isLeader).findFirst().orElse(null);
    }
}
