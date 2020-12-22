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

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.exception.HmilyException;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyZookeeperConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.HmilyRepositoryNode;
import org.dromara.hmily.repository.spi.entity.HmilyLock;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.spi.HmilySPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper impl.
 *
 * @author xiaoyu
 * @author lilang
 */
@HmilySPI("zookeeper")
@Slf4j
public class ZookeeperRepository implements HmilyRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRepository.class);
    
    private static final CountDownLatch LATCH = new CountDownLatch(1);
    
    private static volatile ZooKeeper zooKeeper;
    
    private HmilySerializer hmilySerializer;

    private HmilyRepositoryNode node;

    private String appName;

    @Override
    public void init(final String appName) {
        this.appName = appName;
        this.node = new HmilyRepositoryNode(appName);
        HmilyZookeeperConfig zookeeperConfig = ConfigEnv.getInstance().getConfig(HmilyZookeeperConfig.class);
        try {
            connect(zookeeperConfig);
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
    public int createHmilyTransaction(final HmilyTransaction hmilyTransaction) throws HmilyRepositoryException {
        String path = node.getHmilyTransactionRealPath(hmilyTransaction.getTransId());
        try {
            create(node.getHmilyTransactionRootPath());
            Stat stat = zooKeeper.exists(path, false);
            hmilyTransaction.setAppName(appName);
            if (stat == null) {
                hmilyTransaction.setRetry(0);
                hmilyTransaction.setVersion(0);
                hmilyTransaction.setCreateTime(new Date());
                hmilyTransaction.setUpdateTime(new Date());
                zooKeeper.create(path, hmilySerializer.serialize(hmilyTransaction), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                hmilyTransaction.setVersion(hmilyTransaction.getVersion() + 1);
                hmilyTransaction.setUpdateTime(new Date());
                zooKeeper.setData(path, hmilySerializer.serialize(hmilyTransaction), stat.getVersion());
            }
            return HmilyRepository.ROWS;
        } catch (KeeperException | InterruptedException e) {
            throw new HmilyException(e);
        }
    }

    @Override
    public int updateRetryByLock(final HmilyTransaction hmilyTransaction) {
        final int currentVersion = hmilyTransaction.getVersion();
        String path = node.getHmilyTransactionRealPath(hmilyTransaction.getTransId());
        try {
            if (checkPath(path, true)) {
                return FAIL_ROWS;
            }
            Stat stat = zooKeeper.exists(path, false);
            if (stat == null) {
                LOGGER.warn("path {} is not exists.", path);
                return HmilyRepository.FAIL_ROWS;
            }
            if (currentVersion != stat.getVersion()) {
                LOGGER.warn("current transaction data version different from zookeeper server. "
                        + "current version: {}, server data version:  {}", currentVersion, stat.getVersion());
            }
            hmilyTransaction.setVersion(currentVersion + 1);
            hmilyTransaction.setRetry(hmilyTransaction.getRetry() + 1);
            hmilyTransaction.setUpdateTime(new Date());
            zooKeeper.setData(path, hmilySerializer.serialize(hmilyTransaction), stat.getVersion());
            return HmilyRepository.ROWS;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("updateRetryByLock occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public HmilyTransaction findByTransId(final Long transId) {
        String path = node.getHmilyTransactionRealPath(transId);
        try {
            if (checkPath(path, true)) {
                return null;
            }
            byte[] data = zooKeeper.getData(path, false, null);
            if (data == null) {
                return null;
            }
            return hmilySerializer.deSerialize(data, HmilyTransaction.class);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("findByTransId occur a exception", e);
        }
        return null;
    }

    @Override
    public List<HmilyTransaction> listLimitByDelay(final Date date, final int limit) {
        String path = node.getHmilyTransactionRootPath();
        return listByFilter(path, HmilyTransaction.class, (hmilyTransaction, params) -> {
            Date dateParam = (Date) params[0];
            int limitParam = (int) params[1];
            boolean filterResult = dateParam.after(hmilyTransaction.getUpdateTime())
                    && appName.equals(hmilyTransaction.getAppName())
                    && limitParam-- > 0;
            // write back to params
            params[1] = limitParam;
            return filterResult;
        }, date, limit);
    }

    @Override
    public int updateHmilyTransactionStatus(final Long transId, final Integer status) throws HmilyRepositoryException {
        String path = node.getHmilyTransactionRealPath(transId);
        Stat stat = new Stat();
        try {
            if (checkPath(path, true)) {
                return FAIL_ROWS;
            }
            byte[] data = zooKeeper.getData(path, false, stat);
            if (data == null) {
                return HmilyRepository.FAIL_ROWS;
            }
            HmilyTransaction hmilyTransaction = hmilySerializer.deSerialize(data, HmilyTransaction.class);
            hmilyTransaction.setStatus(status);
            hmilyTransaction.setVersion(hmilyTransaction.getVersion() + 1);
            hmilyTransaction.setUpdateTime(new Date());
            zooKeeper.setData(path, hmilySerializer.serialize(hmilyTransaction), stat.getVersion());
            return HmilyRepository.ROWS;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("updateHmilyTransactionStatus occur a exception", e);
            return HmilyRepository.FAIL_ROWS;
        }
    }

    @Override
    public int removeHmilyTransaction(final Long transId) {
        String path = node.getHmilyTransactionRealPath(transId);
        try {
            if (checkPath(path, false)) {
                return FAIL_ROWS;
            }
            zooKeeper.delete(path, -1);
            return HmilyRepository.ROWS;
        } catch (InterruptedException | KeeperException e) {
            LOGGER.error("removeHmilyTransaction occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyTransactionByDate(final Date date) {
        String path = node.getHmilyTransactionRootPath();
        return removeByFilter(path, HmilyTransaction.class, (hmilyTransaction, params) -> {
            Date dateParam = (Date) params[0];
            return dateParam.after(hmilyTransaction.getUpdateTime()) && hmilyTransaction.getStatus() == HmilyActionEnum.DELETE.getCode();
        }, date);
    }
    
    @Override
    public int createHmilyParticipant(final HmilyParticipant hmilyParticipant) throws HmilyRepositoryException {
        try {
            String path = node.getHmilyParticipantRealPath(hmilyParticipant.getParticipantId());
            create(node.getHmilyParticipantUndoRootPath());
            Stat stat = zooKeeper.exists(path, false);
            hmilyParticipant.setAppName(appName);
            if (stat == null) {
                hmilyParticipant.setRetry(0);
                hmilyParticipant.setVersion(0);
                hmilyParticipant.setCreateTime(new Date());
                hmilyParticipant.setUpdateTime(new Date());
                zooKeeper.create(path, hmilySerializer.serialize(hmilyParticipant), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                hmilyParticipant.setVersion(hmilyParticipant.getVersion() + 1);
                hmilyParticipant.setUpdateTime(new Date());
                zooKeeper.setData(path, hmilySerializer.serialize(hmilyParticipant), stat.getVersion());
            }
            return HmilyRepository.ROWS;
        } catch (KeeperException | InterruptedException e) {
            throw new HmilyException(e);
        }
    }

    @Override
    public List<HmilyParticipant> findHmilyParticipant(final Long participantId) {
        String path = node.getHmilyParticipantRootPath();
        return listByFilter(path, HmilyParticipant.class, (hmilyParticipant, params) -> {
            Long participantIdParam = (Long) params[0];
            return participantIdParam.compareTo(hmilyParticipant.getParticipantId()) == 0
                    || (hmilyParticipant.getParticipantRefId() != null && participantIdParam.compareTo(hmilyParticipant.getParticipantRefId()) == 0);
        }, participantId);
    }

    @Override
    public List<HmilyParticipant> listHmilyParticipant(final Date date, final String transType, final int limit) {
        String path = node.getHmilyParticipantRootPath();
        return listByFilter(path, HmilyParticipant.class, (hmilyParticipant, params) -> {
            Date dateParam = (Date) params[0];
            String transTypeParam = (String) params[1];
            int limitParam = (int) params[2];
            boolean filterResult = dateParam.after(hmilyParticipant.getUpdateTime()) && appName.equals(hmilyParticipant.getAppName())
                    && transTypeParam.equals(hmilyParticipant.getTransType())
                    && (hmilyParticipant.getStatus().compareTo(HmilyActionEnum.DELETE.getCode()) != 0 && hmilyParticipant.getStatus().compareTo(HmilyActionEnum.DEATH.getCode()) != 0)
                    && limitParam-- > 0;
            params[2] = limitParam;
            return filterResult;
        }, date, transType, limit);
    }

    @Override
    public List<HmilyParticipant> listHmilyParticipantByTransId(final Long transId) {
        String path = node.getHmilyParticipantRootPath();
        return listByFilter(path, HmilyParticipant.class, (hmilyParticipant, params) -> transId.compareTo(hmilyParticipant.getTransId()) == 0, transId);
    }

    @Override
    public boolean existHmilyParticipantByTransId(final Long transId) {
        String path = node.getHmilyParticipantRootPath();
        return existByFilter(path, HmilyParticipant.class, (hmilyParticipant, params) -> {
            Long transIdParam = (Long) params[0];
            return transIdParam.compareTo(hmilyParticipant.getTransId()) == 0;
        }, transId);
    }
    
    @Override
    public int updateHmilyParticipantStatus(final Long participantId, final Integer status) throws HmilyRepositoryException {
        String path = node.getHmilyParticipantRealPath(participantId);
        try {
            if (checkPath(path, false)) {
                return FAIL_ROWS;
            }
            Stat stat = new Stat();
            byte[] data = zooKeeper.getData(path, false, stat);
            if (data == null) {
                return HmilyRepository.FAIL_ROWS;
            }
            HmilyParticipant hmilyParticipant = hmilySerializer.deSerialize(data, HmilyParticipant.class);
            hmilyParticipant.setStatus(status);
            hmilyParticipant.setVersion(hmilyParticipant.getVersion() + 1);
            hmilyParticipant.setUpdateTime(new Date());
            zooKeeper.setData(path, hmilySerializer.serialize(hmilyParticipant), stat.getVersion());
            return HmilyRepository.ROWS;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("updateHmilyParticipantStatus occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyParticipant(final Long participantId) {
        String path = node.getHmilyParticipantRealPath(participantId);
        try {
            if (checkPath(path, false)) {
                return FAIL_ROWS;
            }
            zooKeeper.delete(path, -1);
            return HmilyRepository.ROWS;
        } catch (InterruptedException | KeeperException e) {
            LOGGER.error("removeHmilyParticipant occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyParticipantByDate(final Date date) {
        String path = node.getHmilyParticipantRootPath();
        return removeByFilter(path, HmilyParticipant.class, (hmilyParticipant, params) -> {
            Date dateParam = (Date) params[0];
            return dateParam.after(hmilyParticipant.getUpdateTime()) && hmilyParticipant.getStatus().compareTo(HmilyActionEnum.DELETE.getCode()) == 0;
        }, date);
    }

    @Override
    public boolean lockHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        final int currentVersion = hmilyParticipant.getVersion();
        String path = node.getHmilyParticipantRealPath(hmilyParticipant.getParticipantId());
        try {
            if (checkPath(path, false)) {
                return false;
            }
            Stat stat = zooKeeper.exists(path, false);
            if (stat == null) {
                LOGGER.warn("path {} is not exists.", path);
                return false;
            }
            if (currentVersion != stat.getVersion()) {
                LOGGER.warn("current transaction participant data version different from zookeeper server. "
                        + "current version: {}, server data version:  {}", currentVersion, stat.getVersion());
            }
            hmilyParticipant.setVersion(currentVersion + 1);
            hmilyParticipant.setRetry(hmilyParticipant.getRetry() + 1);
            hmilyParticipant.setUpdateTime(new Date());
            zooKeeper.setData(path, hmilySerializer.serialize(hmilyParticipant), stat.getVersion());
            return true;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("updateRetryByLock occur a exception", e);
        }
        return false;
    }

    @Override
    public int createHmilyParticipantUndo(final HmilyParticipantUndo hmilyParticipantUndo) {
        String path = node.getHmilyParticipantUndoRealPath(hmilyParticipantUndo.getUndoId());
        try {
            create(node.getHmilyParticipantUndoRootPath());
            Stat stat = zooKeeper.exists(path, false);
            if (stat == null) {
                hmilyParticipantUndo.setCreateTime(new Date());
                hmilyParticipantUndo.setUpdateTime(new Date());
                zooKeeper.create(path, hmilySerializer.serialize(hmilyParticipantUndo), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                hmilyParticipantUndo.setUpdateTime(new Date());
                zooKeeper.setData(path, hmilySerializer.serialize(hmilyParticipantUndo), stat.getVersion());
            }
            return HmilyRepository.ROWS;
        } catch (KeeperException | InterruptedException e) {
            throw new HmilyException(e);
        }
    }

    @Override
    public List<HmilyParticipantUndo> findHmilyParticipantUndoByParticipantId(final Long participantId) {
        String path = node.getHmilyParticipantUndoRootPath();
        return listByFilter(path, HmilyParticipantUndo.class, (undo, params) -> {
            Long participantIdParam = (Long) params[0];
            return participantIdParam.compareTo(undo.getParticipantId()) == 0;
        }, participantId);
    }

    @Override
    public int removeHmilyParticipantUndo(final Long undoId) {
        String path = node.getHmilyParticipantUndoRealPath(undoId);
        try {
            if (checkPath(path, false)) {
                return FAIL_ROWS;
            }
            zooKeeper.delete(path, -1);
            return HmilyRepository.ROWS;
        } catch (InterruptedException | KeeperException e) {
            LOGGER.error("removeHmilyParticipantUndo occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyParticipantUndoByDate(final Date date) {
        String path = node.getHmilyParticipantUndoRootPath();
        return removeByFilter(path, HmilyParticipantUndo.class, (undo, params) -> {
            Date dateParam = (Date) params[0];
            return dateParam.after(undo.getUpdateTime()) && undo.getStatus().compareTo(HmilyActionEnum.DELETE.getCode()) == 0;
        }, date);
    }

    @Override
    public int updateHmilyParticipantUndoStatus(final Long undoId, final Integer status) {
        String path = node.getHmilyParticipantUndoRealPath(undoId);
        try {
            if (checkPath(path, false)) {
                return FAIL_ROWS;
            }
            Stat stat = new Stat();
            byte[] data = zooKeeper.getData(path, false, stat);
            if (data == null) {
                return HmilyRepository.FAIL_ROWS;
            }
            HmilyParticipantUndo hmilyParticipantUndo = hmilySerializer.deSerialize(data, HmilyParticipantUndo.class);
            hmilyParticipantUndo.setStatus(status);
            hmilyParticipantUndo.setUpdateTime(new Date());
            zooKeeper.setData(path, hmilySerializer.serialize(hmilyParticipantUndo), stat.getVersion());
            return HmilyRepository.ROWS;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("updateHmilyParticipantStatus occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }
    
    @Override
    public int writeHmilyLocks(final Collection<HmilyLock> locks) {
        // TODO
        return 0;
    }
    
    @Override
    public int releaseHmilyLocks(final Collection<HmilyLock> locks) {
        // TODO
        return 0;
    }
    
    @Override
    public Optional<HmilyLock> findHmilyLockById(final String lockId) {
        // TODO
        return Optional.empty();
    }
    
    private void connect(final HmilyZookeeperConfig config) {
        try {
            zooKeeper = new ZooKeeper(config.getHost(), config.getSessionTimeOut(), watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    LATCH.countDown();
                }
            });
            LATCH.await();
            Stat stat = zooKeeper.exists(node.getRootPathPrefix(), false);
            if (stat == null) {
                zooKeeper.create(node.getRootPathPrefix(), node.getRootPathPrefix().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            throw new HmilyRuntimeException(e);
        }
    }

    private boolean checkPath(final String path, final boolean needCreate) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, false);
        if (stat != null) {
            return false;
        }
        if (needCreate) {
            create(path);
        }
        return !needCreate;
    }

    private void create(final String path) throws KeeperException, InterruptedException {
        PathTokenizer pathTokenizer = new PathTokenizer(path);
        while (pathTokenizer.hasNext()) {
            String nextPath = pathTokenizer.nextPath();
            if (StringUtils.isNoneBlank(path)) {
                Stat stat = zooKeeper.exists(nextPath, false);
                if (stat == null) {
                    zooKeeper.create(nextPath, nextPath.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            }
        }
    }

    private <T> List<T> listByFilter(final String path, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            if (checkPath(path, false)) {
                return Collections.emptyList();
            }
            List<String> children = zooKeeper.getChildren(path, false);
            if (CollectionUtils.isEmpty(children)) {
                return Collections.emptyList();
            }
            List<T> result = new ArrayList<>();
            for (String child : children) {
                byte[] data = zooKeeper.getData(path + "/" + child, false, null);
                if (data == null) {
                    continue;
                }
                T t = hmilySerializer.deSerialize(data, deserializeClass);
                if (filter.filter(t, params)) {
                    result.add(t);
                }
            }
            return result;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("listByFilter occur a exception", e);
        }
        return Collections.emptyList();
    }

    private <T> boolean existByFilter(final String path, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            if (checkPath(path, false)) {
                return false;
            }
            List<String> children = zooKeeper.getChildren(path, false);
            if (CollectionUtils.isEmpty(children)) {
                return false;
            }
            for (String child : children) {
                byte[] data = zooKeeper.getData(path + "/" + child, false, null);
                if (data == null) {
                    continue;
                }
                T t = hmilySerializer.deSerialize(data, deserializeClass);
                if (filter.filter(t, params)) {
                    return true;
                }
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("existByFilter occur a exception", e);
        }
        return false;
    }

    private <T> int removeByFilter(final String path, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            if (checkPath(path, false)) {
                return FAIL_ROWS;
            }
            List<String> children = zooKeeper.getChildren(path, false);
            if (CollectionUtils.isEmpty(children)) {
                return HmilyRepository.FAIL_ROWS;
            }

            int count = 0;
            for (String child : children) {
                Stat stat = new Stat();
                byte[] data = zooKeeper.getData(path + "/" + child, false, stat);
                if (data == null) {
                    continue;
                }
                T t = hmilySerializer.deSerialize(data, deserializeClass);
                if (filter.filter(t, params)) {
                    zooKeeper.delete(path + "/" + child, stat.getVersion());
                    count++;
                }
            }
            return count;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("removeByFilter occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }
    
    /**
     * The type Path tokenizer.
     */
    static class PathTokenizer {
        
        private String path = "";
        
        private String[] nodes;
        
        private int index;
    
        /**
         * Instantiates a new Path tokenizer.
         *
         * @param path the path
         */
        PathTokenizer(final String path) {
            if (path == null) {
                throw new IllegalArgumentException("path cannot be null.");
            }
            nodes = path.split("/");
            if (path.startsWith("/")) {
                index = 1;
            }
        }
    
        /**
         * Next path string.
         *
         * @return the string
         */
        public String nextPath() {
            path = path + "/" + nodes[index];
            index++;
            return path;
        }
    
        /**
         * Has next boolean.
         *
         * @return the boolean
         */
        public boolean hasNext() {
            return index < nodes.length;
        }
    }
    
    /**
     * The interface Filter.
     *
     * @param <T> the type parameter
     */
    interface Filter<T> {
    
        /**
         * Filter boolean.
         *
         * @param t      the t
         * @param params the params
         * @return the boolean
         */
        boolean filter(T t, Object... params);
    }
}
