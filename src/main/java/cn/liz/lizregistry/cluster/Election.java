package cn.liz.lizregistry.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Election {
    public void electLeader(List<Server> servers) {
        List<Server> masters = servers.stream()
                .filter(Server::isStatus).filter(Server::isLeader).collect(Collectors.toList());
        if (masters.isEmpty()) {
            log.warn("=== ########## elect with no leader, servers:{}", servers);
            elect(servers);
        } else if (masters.size() > 1) {
            log.warn("=== !!!!!!!!!! elect leader start, more than one leader:{}", servers);
            elect(servers);
        } else {
            log.debug("=== ~~~~~~ elect leader return, already existed leader:{}", masters.get(0));
        }
    }

    private void elect(List<Server> servers) {
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
}
