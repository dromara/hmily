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

package org.dromara.hmily.repository.zookeeper;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.config.HmilyConfig;
import org.dromara.hmily.config.HmilyZookeeperConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * zookeeper impl.
 *
 * @author xiaoyu
 */
public class ZookeeperRepository implements HmilyRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRepository.class);
    
    private static volatile ZooKeeper zooKeeper;
    
    private static final CountDownLatch LATCH = new CountDownLatch(1);
    
    private HmilySerializer hmilySerializer;
    
    private String rootPathPrefix = "/hmily";
    
    
    @Override
    public void init(final HmilyConfig hmilyConfig) {
        rootPathPrefix = hmilyConfig.getAppName();
        try {
            connect(hmilyConfig.getHmilyZookeeperConfig());
        } catch (Exception e) {
            LogUtil.error(LOGGER, "zookeeper init error please check you config:{}", e::getMessage);
            throw new HmilyRuntimeException(e.getMessage());
        }
    }
    
    @Override
    public void setSerializer(final HmilySerializer hmilySerializer) {
        this.hmilySerializer = hmilySerializer;
    }
    
    @Override
    public int createHmilyTransaction(HmilyTransaction hmilyTransaction) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public int updateRetryByLock(HmilyTransaction hmilyTransaction) {
        return 0;
    }
    
    @Override
    public HmilyTransaction findByTransId(String transId) {
        return null;
    }
    
    @Override
    public List<HmilyTransaction> listLimitByDelay(Date date, int limit) {
        return null;
    }
    
    @Override
    public int updateHmilyTransactionStatus(String transId, Integer status) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public int removeHmilyTransaction(String transId) {
        return 0;
    }
    
    @Override
    public int createHmilyParticipant(HmilyParticipant hmilyParticipant) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public List<HmilyParticipant> findHmilyParticipant(String participantId) {
        return null;
    }
    
    @Override
    public List<HmilyParticipant> listHmilyParticipant(Date date, int limit) {
        return null;
    }
    
    @Override
    public List<HmilyParticipant> listHmilyParticipantByTransId(String transId) {
        return null;
    }
    
    @Override
    public boolean existHmilyParticipantByTransId(String transId) {
        return false;
    }
    
    @Override
    public int updateHmilyParticipantStatus(String participantId, Integer status) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public int removeHmilyParticipant(String participantId) {
        return 0;
    }
    
    @Override
    public boolean lockHmilyParticipant(HmilyParticipant hmilyParticipant) {
        return false;
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
}
