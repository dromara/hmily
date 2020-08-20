package org.dromara.hmily.repository.zookeeper.mock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.DataNode;
import org.apache.zookeeper.server.DataTree;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;


/**
 * Author:   lilang
 * Date:     2020-08-19 22:18
 * Description:
 **/
public class ZookeeperMock {

    private DataTree dataTree = new DataTree();

    private ZooKeeper zooKeeper = Mockito.mock(ZooKeeper.class);

    public ZooKeeper getZooKeeper() {
        return this.zooKeeper;
    }


    public void mockCreate() throws KeeperException, InterruptedException {
        when(zooKeeper.create(anyString(), any(byte[].class), anyList(), eq(CreateMode.PERSISTENT))).then(x -> {
            String path = x.getArgument(0);
            byte[] data = x.getArgument(1);
            List<ACL> aclList = x.getArgument(2);
            // ephemeralOwner == 0 means permanent node
            dataTree.createNode(path, data, aclList, 0, 0, 0, System.currentTimeMillis());
            return "";
        });
    }

    public void mockSetData() throws KeeperException, InterruptedException {
        when(zooKeeper.setData(anyString(), any(byte[].class), anyInt())).then(x -> {
            String path = x.getArgument(0);
            byte[] data = x.getArgument(1);
            int version = x.getArgument(2);
            checkVersion(path, version);
            return dataTree.setData(path, data, version, 0, System.currentTimeMillis());
        });
    }

    public void mockExists() throws KeeperException, InterruptedException {
        when(zooKeeper.exists(anyString(), eq(false))).then(x -> {
            try {
                return dataTree.statNode(x.getArgument(0), null);
            } catch (KeeperException.NoNodeException e) {
                return null;
            }
        });
    }

    public void mockGetChildren() throws KeeperException, InterruptedException {
        when(zooKeeper.getChildren(anyString(), eq(false))).then(x ->
                dataTree.getChildren(x.getArgument(0), null, null)
        );
    }

    public void mockGetData() throws KeeperException, InterruptedException {
        when(zooKeeper.getData(anyString(), eq(false), any()))
                .then(x ->
                        dataTree.getData(x.getArgument(0), x.getArgument(2) == null ? new Stat() : x.getArgument(2), null));
    }

    public void mockDelete() throws KeeperException, InterruptedException {
        // return is void and mock the method
        doAnswer(x -> {
            String path = x.getArgument(0);
            int version = x.getArgument(1);
            checkVersion(path, version);
            dataTree.deleteNode(path, 0);
            return this;
        }).when(zooKeeper).delete(anyString(), anyInt());
    }

    private void checkVersion(String path, int version) throws KeeperException.BadVersionException {
        DataNode node = dataTree.getNode(path);
        if (node != null) {
            if (version != -1 && node.stat.getVersion() != version) {
                throw new KeeperException.BadVersionException();
            }
        }
    }

}
