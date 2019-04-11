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

package org.dromara.hmily.core.spi.repository;

import com.google.common.collect.Lists;
import org.dromara.hmily.annotation.HmilySPI;
import org.dromara.hmily.common.bean.adapter.CoordinatorRepositoryAdapter;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.config.HmilyConfig;
import org.dromara.hmily.common.config.HmilyZookeeperConfig;
import org.dromara.hmily.common.enums.RepositorySupportEnum;
import org.dromara.hmily.common.exception.HmilyException;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.serializer.ObjectSerializer;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.utils.RepositoryConvertUtils;
import org.dromara.hmily.common.utils.RepositoryPathUtils;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.dromara.hmily.core.spi.HmilyCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * zookeeper impl.
 *
 * @author xiaoyu
 */
@HmilySPI("zookeeper")
public class ZookeeperCoordinatorRepository implements HmilyCoordinatorRepository {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperCoordinatorRepository.class);

    private static volatile ZooKeeper zooKeeper;

    private static final CountDownLatch LATCH = new CountDownLatch(1);

    private ObjectSerializer objectSerializer;

    private String rootPathPrefix = "/hmily";

    @Override
    public int create(final HmilyTransaction hmilyTransaction) {
        try {
            zooKeeper.create(buildRootPath(hmilyTransaction.getTransId()),
                    RepositoryConvertUtils.convert(hmilyTransaction, objectSerializer),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            return ROWS;
        } catch (Exception e) {
            throw new HmilyRuntimeException(e);
        }
    }

    @Override
    public int remove(final String id) {
        try {
            zooKeeper.delete(buildRootPath(id), -1);
            return ROWS;
        } catch (Exception e) {
            throw new HmilyRuntimeException(e);
        }
    }

    @Override
    public int update(final HmilyTransaction hmilyTransaction) throws HmilyRuntimeException {
        try {
            hmilyTransaction.setLastTime(new Date());
            hmilyTransaction.setVersion(hmilyTransaction.getVersion() + 1);
            zooKeeper.setData(buildRootPath(hmilyTransaction.getTransId()),
                    RepositoryConvertUtils.convert(hmilyTransaction, objectSerializer), -1);
            return ROWS;
        } catch (Exception e) {
            throw new HmilyRuntimeException(e);
        }
    }

    @Override
    public int updateParticipant(final HmilyTransaction hmilyTransaction) {
        final String path = RepositoryPathUtils.buildZookeeperRootPath(rootPathPrefix, hmilyTransaction.getTransId());
        try {
            byte[] content = zooKeeper.getData(path, false, new Stat());
            if (content != null) {
                final CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
                adapter.setContents(objectSerializer.serialize(hmilyTransaction.getHmilyParticipants()));
                zooKeeper.setData(path, objectSerializer.serialize(adapter), -1);
            }
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
            if (content != null) {
                final CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
                adapter.setStatus(status);
                zooKeeper.setData(path, objectSerializer.serialize(adapter), -1);
            }
            return ROWS;
        } catch (Exception e) {
            e.printStackTrace();
            return FAIL_ROWS;
        }
    }

    @Override
    public HmilyTransaction findById(final String id) {
        try {
            Stat stat = new Stat();
            byte[] content = zooKeeper.getData(buildRootPath(id), false, stat);
            return RepositoryConvertUtils.transformBean(content, objectSerializer);
        } catch (Exception e) {
            throw new HmilyRuntimeException(e);
        }
    }

    @Override
    public List<HmilyTransaction> listAll() {
        List<HmilyTransaction> transactionRecovers = Lists.newArrayList();
        List<String> zNodePaths;
        try {
            zNodePaths = zooKeeper.getChildren(rootPathPrefix, false);
        } catch (Exception e) {
            throw new HmilyRuntimeException(e);
        }
        if (CollectionUtils.isNotEmpty(zNodePaths)) {
            transactionRecovers = zNodePaths.stream()
                    .filter(StringUtils::isNoneBlank)
                    .map(zNodePath -> {
                        try {
                            byte[] content = zooKeeper.getData(buildRootPath(zNodePath), false, new Stat());
                            return RepositoryConvertUtils.transformBean(content, objectSerializer);
                        } catch (KeeperException | InterruptedException | HmilyException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).collect(Collectors.toList());
        }
        return transactionRecovers;
    }

    @Override
    public List<HmilyTransaction> listAllByDelay(final Date date) {
        final List<HmilyTransaction> hmilyTransactions = listAll();
        return hmilyTransactions.stream()
                .filter(transaction -> transaction.getLastTime().compareTo(date) > 0)
                .collect(Collectors.toList());
    }

    @Override
    public void init(final String modelName, final HmilyConfig hmilyConfig) {
        rootPathPrefix = RepositoryPathUtils.buildZookeeperPathPrefix(modelName);
        try {
            connect(hmilyConfig.getHmilyZookeeperConfig());
        } catch (Exception e) {
            LogUtil.error(LOGGER, "zookeeper init error please check you config:{}", e::getMessage);
            throw new HmilyRuntimeException(e.getMessage());
        }
    }

    private void connect(final HmilyZookeeperConfig config) {
        try {
            zooKeeper = new ZooKeeper(config.getHost(), config.getSessionTimeOut(), watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    LATCH.countDown();
                }
            });
            LATCH.await();
            Stat stat = zooKeeper.exists(rootPathPrefix, false);
            if (stat == null) {
                zooKeeper.create(rootPathPrefix, rootPathPrefix.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            throw new HmilyRuntimeException(e);
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
