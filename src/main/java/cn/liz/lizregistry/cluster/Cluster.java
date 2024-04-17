package cn.liz.lizregistry.cluster;

import cn.liz.lizregistry.config.LizRegistryConfigProperties;
import cn.liz.lizregistry.http.HttpInvoker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    long timeout = 5_000;

    public void init() {
        host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
        log.info("====== findFirstNonLoopbackHostInfo :{}", host);

        MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);
        log.info("====== myself:{}", MYSELF);


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

        executorService.scheduleAtFixedRate(() -> {
            try {
                updateServers();
                electLeader();
            } catch (Exception e) {
                log.warn("cluster scheduleAtFixedRate err, e : ", e);
            }
        }, 0, timeout, TimeUnit.MILLISECONDS);

    }

    private void electLeader() {
        List<Server> masters = this.servers.stream()
                .filter(Server::isStatus).filter(Server::isLeader).collect(Collectors.toList());
        if (masters.isEmpty()) {
            log.info("=== ########## elect with no leader, servers:{}", servers);
            elect();
        } else if (masters.size() > 1) {
            log.info("=== !!!!!!!!!! elect leader start, more than one leader:{}", servers);
            elect();
        } else {
            log.info("=== ~~~~~~ elect leader return, already existed leader:{}", masters.get(0));
        }
    }

    private void elect() {
        // 三种方式：
        // 1.各节点自己选，算法保证选出来的是同一个
        // 2.用分布式锁，谁拿到锁谁是leader
        // 3.用分布式一致性算法，paxos、raft

        Server candidate = null;
        for (Server server : servers) {
            server.setLeader(false);
            if (server.isStatus()) {
                if (candidate == null) {
                    candidate = server;
                } else {
                    if (candidate.hashCode() > server.hashCode()) {
                        candidate = server;
                    }
                }
            }
        }
        if (candidate != null) {
            candidate.setLeader(true);
            log.info("=== elect finished, leader:{}", candidate);
        } else {
            log.warn("=== elect fail, servers:{}", servers);
        }
    }

    private void updateServers() {
        servers.forEach(server -> {
            try {
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                log.info("====== health check result for serverInfo:{}", serverInfo);
                if (serverInfo != null) {
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                }
            } catch (Exception e) {
                log.warn("====== health check fail for server:{}", server);
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }

    public Server self() {
        return MYSELF;
    }

    public List<Server> getServers() {
        return servers;
    }

    public Server leader() {
        return this.servers.stream().filter(Server::isStatus).filter(Server::isLeader).findFirst().orElse(null);
    }
}
