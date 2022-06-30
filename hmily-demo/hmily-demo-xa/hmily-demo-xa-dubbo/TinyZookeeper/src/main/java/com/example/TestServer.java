package com.example;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.TestingServer;
import org.apache.curator.test.TestingZooKeeperServer;

public class TestServer {
    public static void main(String[] args) throws Exception {
        //可以模拟单机或者集群模式的zk服务器环境
//        testStandalone();
//        集群
//        testCluster();
        TestingServer server = new TestingServer(2181);
    }

    private static void testStandalone() throws Exception {
        TestingServer server = new TestingServer(2181);
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(15000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(15000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
    }

    private static void testCluster() throws Exception {
        TestingCluster cluster = new TestingCluster(3);
        cluster.start();
        Thread.sleep(5000);
        for (TestingZooKeeperServer server : cluster.getServers()) {
            System.out.println(server.getInstanceSpec().getServerId() + " - " + server.getQuorumPeer().getServerState());
        }
        cluster.getServers().get(0).kill();

        for (TestingZooKeeperServer server : cluster.getServers()) {
            System.out.println(server.getInstanceSpec().getServerId() + " - " + server.getQuorumPeer().getServerState());
        }

        cluster.stop();
    }
}
