/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hmily.tcc.core.spi.repository;

import com.google.common.collect.Lists;
import com.hmily.tcc.common.bean.adapter.CoordinatorRepositoryAdapter;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.config.TccConfig;
import com.hmily.tcc.common.config.TccZookeeperConfig;
import com.hmily.tcc.common.enums.RepositorySupportEnum;
import com.hmily.tcc.common.exception.TccException;
import com.hmily.tcc.common.exception.TccRuntimeException;
import com.hmily.tcc.common.serializer.ObjectSerializer;
import com.hmily.tcc.common.utils.LogUtil;
import com.hmily.tcc.common.utils.RepositoryConvertUtils;
import com.hmily.tcc.common.utils.RepositoryPathUtils;
import com.hmily.tcc.core.spi.CoordinatorRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * zookeeper impl.
 * @author xiaoyu
 */
public class ZookeeperCoordinatorRepository implements CoordinatorRepository {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperCoordinatorRepository.class);

    private static volatile ZooKeeper zooKeeper;

    private static final CountDownLatch LATCH = new CountDownLatch(1);

    private ObjectSerializer objectSerializer;

    private String rootPathPrefix = "/hmily";

    @Override
    public int create(final TccTransaction tccTransaction) {
        try {
            zooKeeper.create(buildRootPath(tccTransaction.getTransId()),
                    RepositoryConvertUtils.convert(tccTransaction, objectSerializer),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            return ROWS;
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    @Override
    public int remove(final String id) {
        try {
            zooKeeper.delete(buildRootPath(id), -1);
            return ROWS;
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    @Override
    public int update(final TccTransaction tccTransaction) throws TccRuntimeException {
        try {
            tccTransaction.setLastTime(new Date());
            tccTransaction.setVersion(tccTransaction.getVersion() + 1);
            zooKeeper.setData(buildRootPath(tccTransaction.getTransId()),
                    RepositoryConvertUtils.convert(tccTransaction, objectSerializer), -1);
            return ROWS;
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    @Override
    public int updateParticipant(final TccTransaction tccTransaction) {
        final String path = RepositoryPathUtils.buildZookeeperRootPath(rootPathPrefix, tccTransaction.getTransId());
        try {
            byte[] content = zooKeeper.getData(path, false, new Stat());
            final CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
            adapter.setContents(objectSerializer.serialize(tccTransaction.getParticipants()));
            zooKeeper.create(path, objectSerializer.serialize(adapter), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            return ROWS;
        } catch (Exception e) {
            e.printStackTrace();
            return FAIL_ROWS;
        }
    }

    @Override
    public int updateStatus(final String id, final Integer status) {
        final String path = RepositoryPathUtils.buildZookeeperRootPath(rootPathPrefix, id);
        try {
            byte[] content = zooKeeper.getData(path, false, new Stat());
            final CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
            adapter.setStatus(status);
            zooKeeper.create(path, objectSerializer.serialize(adapter), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            return ROWS;
        } catch (Exception e) {
            e.printStackTrace();
            return FAIL_ROWS;
        }
    }

    @Override
    public TccTransaction findById(final String id) {
        try {
            Stat stat = new Stat();
            byte[] content = zooKeeper.getData(buildRootPath(id), false, stat);
            return RepositoryConvertUtils.transformBean(content, objectSerializer);
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    @Override
    public List<TccTransaction> listAll() {
        List<TccTransaction> transactionRecovers = Lists.newArrayList();
        List<String> zNodePaths;
        try {
            zNodePaths = zooKeeper.getChildren(rootPathPrefix, false);
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
        if (CollectionUtils.isNotEmpty(zNodePaths)) {
            transactionRecovers = zNodePaths.stream()
                    .filter(StringUtils::isNoneBlank)
                    .map(zNodePath -> {
                        try {
                            byte[] content = zooKeeper.getData(buildRootPath(zNodePath), false, new Stat());
                            return RepositoryConvertUtils.transformBean(content, objectSerializer);
                        } catch (KeeperException | InterruptedException | TccException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).collect(Collectors.toList());
        }
        return transactionRecovers;
    }

    @Override
    public List<TccTransaction> listAllByDelay(final Date date) {
        final List<TccTransaction> tccTransactions = listAll();
        return tccTransactions.stream()
                .filter(tccTransaction -> tccTransaction.getLastTime().compareTo(date) > 0)
                .collect(Collectors.toList());
    }

    @Override
    public void init(final String modelName, final TccConfig tccConfig) {
        rootPathPrefix = RepositoryPathUtils.buildZookeeperPathPrefix(modelName);
        try {
            connect(tccConfig.getTccZookeeperConfig());
        } catch (Exception e) {
            LogUtil.error(LOGGER, "zookeeper连接异常请检查配置信息是否正确:{}", e::getMessage);
            throw new TccRuntimeException(e.getMessage());
        }
    }

    private void connect(final TccZookeeperConfig config) {
        try {
            zooKeeper = new ZooKeeper(config.getHost(), config.getSessionTimeOut(), watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    // 放开闸门, wait在connect方法上的线程将被唤醒
                    LATCH.countDown();
                }
            });
            LATCH.await();
            Stat stat = zooKeeper.exists(rootPathPrefix, false);
            if (stat == null) {
                zooKeeper.create(rootPathPrefix, rootPathPrefix.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    @Override
    public String getScheme() {
        return RepositorySupportEnum.ZOOKEEPER.getSupport();
    }

    @Override
    public void setSerializer(final ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

    private String buildRootPath(final String id) {
        return RepositoryPathUtils.buildZookeeperRootPath(rootPathPrefix, id);
    }
}
