package cn.liz.lizregistry.cluster;

import cn.liz.lizregistry.http.HttpInvoker;
import cn.liz.lizregistry.service.LizRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ServerHealth {

    final Cluster cluster;

    final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    long interval = 5_000;

    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }

    public void checkServerHealth() {
        executorService.scheduleAtFixedRate(() -> {
            try {
                updateServers();//更新注册中心节点的服务状态
                doElect();//选主
                syncSnapshotFromLeader();// 同步快照
            } catch (Exception e) {
                log.warn("cluster scheduleAtFixedRate err, e : ", e);
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

    private void syncSnapshotFromLeader() {
        Server self = cluster.self();
        Server leader = cluster.leader();
        if (self.isLeader() || self.getVersion() >= leader.getVersion()) return;
        log.debug(" === syncSnapshotFromLeader, leader:{}, mySelfVersion:{}", leader, self.getVersion());
        Snapshot snapshot = HttpInvoker.httpGet(leader.getUrl() + "/snapshot", Snapshot.class);
        log.debug(" === syncSnapshotFromLeader, snapshot:{}", snapshot);
        LizRegistryService.restore(snapshot);
    }

    private void doElect() {
        new Election().electLeader(cluster.getServers());
    }

    private void updateServers() {
        List<Server> servers = cluster.getServers();
        servers.stream().parallel().forEach(server -> {
            try {
                if (server.equals(cluster.self())) return;
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                log.debug("====== health check result for serverInfo:{}", serverInfo);
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
}
