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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.Watcher;
import org.dromara.hmily.common.exception.HmilyException;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.HmilyConfig;
import org.dromara.hmily.config.HmilyZookeeperConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.spi.HmilySPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * zookeeper impl.
 *
 * @author xiaoyu
 */
@HmilySPI("zookeeper")
@Slf4j
public class ZookeeperRepository implements HmilyRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRepository.class);

    private static volatile ZooKeeper zooKeeper;

    private static final CountDownLatch LATCH = new CountDownLatch(1);

    private static final String HMILY_TRANSACTION_GLOBAL = "hmily_transaction_global";

    private static final String HMILY_TRANSACTION_PRTICIPANT = "hmily_transaction_participant";

    private static final String HMILY_PARTICIPANT_UNDO = "hmily_participant_undo";

    private HmilySerializer hmilySerializer;

    private String rootPathPrefix = "/hmily";

    private String appName = "default";

    @Override
    public void init(final HmilyConfig hmilyConfig) {
        appName = hmilyConfig.getAppName();
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
    public int createHmilyTransaction(final HmilyTransaction hmilyTransaction) throws HmilyRepositoryException {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_GLOBAL;
        try {
            create(path);
            Stat stat = zooKeeper.exists(path + "/" + hmilyTransaction.getTransId(), false);
            if (stat == null) {
                hmilyTransaction.setRetry(0);
                hmilyTransaction.setVersion(0);
                hmilyTransaction.setCreateTime(new Date());
                hmilyTransaction.setUpdateTime(new Date());
                zooKeeper.create(path + "/" + hmilyTransaction.getTransId(),
                        hmilySerializer.serialize(hmilyTransaction), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                hmilyTransaction.setVersion(hmilyTransaction.getVersion() + 1);
                hmilyTransaction.setUpdateTime(new Date());
                zooKeeper.setData(path + "/" + hmilyTransaction.getTransId(),
                        hmilySerializer.serialize(hmilyTransaction), stat.getVersion());
            }
            return HmilyRepository.ROWS;
        } catch (KeeperException e) {
            throw new HmilyException(e);
        } catch (InterruptedException e) {
            throw new HmilyException(e);
        }
    }

    @Override
    public int updateRetryByLock(final HmilyTransaction hmilyTransaction) {
        final int currentVersion = hmilyTransaction.getVersion();
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_GLOBAL + "/" + hmilyTransaction.getTransId();
        try {
            if (!checkAndInitPath(path, true)) {
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
        } catch (KeeperException e) {
            LOGGER.error("updateRetryByLock occur a exception", e);
        } catch (InterruptedException e) {
            LOGGER.error("updateRetryByLock occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public HmilyTransaction findByTransId(final Long transId) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_GLOBAL + "/" + transId;
        try {
            if (!checkAndInitPath(path, true)) {
                return null;
            }

            byte[] data = zooKeeper.getData(path, false, null);
            if (data == null) {
                return null;
            }
            return hmilySerializer.deSerialize(data, HmilyTransaction.class);
        } catch (KeeperException e) {
            LOGGER.error("findByTransId occur a exception", e);
        } catch (InterruptedException e) {
            LOGGER.error("findByTransId occur a exception", e);
        }

        return null;
    }

    @Override
    public List<HmilyTransaction> listLimitByDelay(final Date date, final int limit) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_GLOBAL;
        List<HmilyTransaction> result = listByFilter(path, HmilyTransaction.class, (hmilyTransaction, params) -> {
            Date dateParam = (Date) params[0];
            int limitParam = (int) params[1];
            boolean filterResult = dateParam.before(hmilyTransaction.getUpdateTime())
                    && appName.equals(hmilyTransaction.getAppName())
                    && limitParam-- > 0;
            // write back to params
            params[1] = limitParam;
            return filterResult;
        }, date, limit);
        return result;
    }

    @Override
    public int updateHmilyTransactionStatus(final Long transId, final Integer status) throws HmilyRepositoryException {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_GLOBAL + "/" + transId;
        Stat stat = new Stat();
        try {
            if (!checkAndInitPath(path, true)) {
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
        } catch (KeeperException e) {
            LOGGER.error("updateHmilyTransactionStatus occur a exception", e);
        } catch (InterruptedException e) {
            LOGGER.error("updateHmilyTransactionStatus occur a exception", e);
        }

        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyTransaction(final Long transId) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_GLOBAL + "/" + transId;
        try {
            if (!checkAndInitPath(path, false)) {
                return FAIL_ROWS;
            }
            zooKeeper.delete(path, -1);
            return HmilyRepository.ROWS;
        } catch (InterruptedException e) {
            LOGGER.error("removeHmilyTransaction occur a exception", e);
        } catch (KeeperException e) {
            LOGGER.error("removeHmilyTransaction occur a exception", e);
        }

        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyTransactionByData(final Date date) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_GLOBAL;
        return removeByFilter(path, HmilyTransaction.class, (hmilyTransaction, params) -> {
            Date dateParam = (Date) params[0];
            return dateParam.before(hmilyTransaction.getUpdateTime()) && hmilyTransaction.getStatus() == 4;
        }, date);
    }

    @Override
    public int createHmilyParticipant(final HmilyParticipant hmilyParticipant) throws HmilyRepositoryException {
        try {
            String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_PRTICIPANT;
            create(path);
            Stat stat = zooKeeper.exists(path + "/" + hmilyParticipant.getTransId(), false);
            if (stat == null) {
                hmilyParticipant.setRetry(0);
                hmilyParticipant.setVersion(0);
                hmilyParticipant.setCreateTime(new Date());
                hmilyParticipant.setUpdateTime(new Date());
                zooKeeper.create(path + "/" + hmilyParticipant.getParticipantId(),
                        hmilySerializer.serialize(hmilyParticipant), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                hmilyParticipant.setVersion(hmilyParticipant.getVersion() + 1);
                hmilyParticipant.setUpdateTime(new Date());
                zooKeeper.setData(path + "/" + hmilyParticipant.getParticipantId(),
                        hmilySerializer.serialize(hmilyParticipant), stat.getVersion());
            }
            return HmilyRepository.ROWS;
        } catch (KeeperException e) {
            throw new HmilyException(e);
        } catch (InterruptedException e) {
            throw new HmilyException(e);
        }
    }

    @Override
    public List<HmilyParticipant> findHmilyParticipant(final Long participantId) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_PRTICIPANT;
        List<HmilyParticipant> result = listByFilter(path, HmilyParticipant.class, (hmilyParticipant, params) -> {
            Long participantIdParam = (Long) params[0];
            return participantIdParam.compareTo(hmilyParticipant.getParticipantId()) == 0
                    || (hmilyParticipant.getParticipantRefId() != null && participantIdParam.compareTo(hmilyParticipant.getParticipantRefId()) == 0);
        }, participantId);
        return result;
    }

    @Override
    public List<HmilyParticipant> listHmilyParticipant(final Date date, final String transType, final int limit) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_PRTICIPANT;
        List<HmilyParticipant> result = listByFilter(path, HmilyParticipant.class, (hmilyParticipant, params) -> {
            Date dateParam = (Date) params[0];
            String transTypeParam = (String) params[1];
            int limitParam = (int) params[2];
            boolean filterResult = dateParam.before(hmilyParticipant.getUpdateTime()) && appName.equals(hmilyParticipant.getAppName())
                    && transTypeParam.equals(hmilyParticipant.getTransType())
                    && (hmilyParticipant.getStatus().compareTo(4) != 0 && hmilyParticipant.getStatus().compareTo(8) != 0)
                    && limitParam-- > 0;
            params[2] = limitParam;
            return filterResult;
        }, date, transType, limit);
        return result;
    }

    @Override
    public List<HmilyParticipant> listHmilyParticipantByTransId(final Long transId) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_PRTICIPANT;
        List<HmilyParticipant> result = listByFilter(path, HmilyParticipant.class,
            (hmilyParticipant, params) -> transId.compareTo(hmilyParticipant.getTransId()) == 0,
            transId);
        return result;
    }

    @Override
    public boolean existHmilyParticipantByTransId(final Long transId) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_PRTICIPANT;
        return existByFilter(path, HmilyParticipant.class, (hmilyParticipant, params) -> {
            Long transIdParam = (Long) params[0];
            return transIdParam.compareTo(hmilyParticipant.getTransId()) == 0;
        }, transId);
    }

    @Override
    public int updateHmilyParticipantStatus(final Long participantId, final Integer status) throws HmilyRepositoryException {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_PRTICIPANT + "/" + participantId;
        try {
            if (!checkAndInitPath(path, false)) {
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
        } catch (KeeperException e) {
            LOGGER.error("updateHmilyParticipantStatus occur a exception", e);
        } catch (InterruptedException e) {
            LOGGER.error("updateHmilyParticipantStatus occur a exception", e);
        }

        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyParticipant(final Long participantId) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_PRTICIPANT + "/" + participantId;
        try {
            if (!checkAndInitPath(path, false)) {
                return FAIL_ROWS;
            }
            zooKeeper.delete(path, -1);
            return HmilyRepository.ROWS;
        } catch (InterruptedException e) {
            LOGGER.error("removeHmilyParticipant occur a exception", e);
        } catch (KeeperException e) {
            LOGGER.error("removeHmilyParticipant occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyParticipantByData(final Date date) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_PRTICIPANT;
        return removeByFilter(path, HmilyParticipant.class, (hmilyParticipant, params) -> {
            Date dateParam = (Date) params[0];
            return dateParam.before(hmilyParticipant.getUpdateTime()) && hmilyParticipant.getStatus().compareTo(4) == 0;
        }, date);
    }

    @Override
    public boolean lockHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        final int currentVersion = hmilyParticipant.getVersion();
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_TRANSACTION_PRTICIPANT + "/" + hmilyParticipant.getParticipantId();
        try {
            if (!checkAndInitPath(path, false)) {
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
        } catch (KeeperException e) {
            LOGGER.error("updateRetryByLock occur a exception", e);
        } catch (InterruptedException e) {
            LOGGER.error("updateRetryByLock occur a exception", e);
        }
        return false;
    }

    @Override
    public int createHmilyParticipantUndo(final HmilyParticipantUndo hmilyParticipantUndo) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_PARTICIPANT_UNDO;
        try {
            create(path);
            Stat stat = zooKeeper.exists(path + "/" + hmilyParticipantUndo.getUndoId(), false);
            if (stat == null) {
                hmilyParticipantUndo.setCreateTime(new Date());
                hmilyParticipantUndo.setUpdateTime(new Date());
                zooKeeper.create(path + "/" + hmilyParticipantUndo.getUndoId(),
                        hmilySerializer.serialize(hmilyParticipantUndo), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                hmilyParticipantUndo.setUpdateTime(new Date());
                zooKeeper.setData(path + "/" + hmilyParticipantUndo.getUndoId(),
                        hmilySerializer.serialize(hmilyParticipantUndo), stat.getVersion());
            }
            return HmilyRepository.ROWS;
        } catch (KeeperException e) {
            throw new HmilyException(e);
        } catch (InterruptedException e) {
            throw new HmilyException(e);
        }
    }

    @Override
    public List<HmilyParticipantUndo> findHmilyParticipantUndoByParticipantId(final Long participantId) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_PARTICIPANT_UNDO;
        return listByFilter(path, HmilyParticipantUndo.class, (undo, params) -> {
            Long participantIdParam = (Long) params[0];
            return participantIdParam.compareTo(undo.getParticipantId()) == 0;
        }, participantId);
    }

    @Override
    public int removeHmilyParticipantUndo(final Long undoId) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_PARTICIPANT_UNDO + "/" + undoId;
        try {
            if (!checkAndInitPath(path, false)) {
                return FAIL_ROWS;
            }
            zooKeeper.delete(path, -1);
            return HmilyRepository.ROWS;
        } catch (InterruptedException e) {
            LOGGER.error("removeHmilyParticipantUndo occur a exception", e);
        } catch (KeeperException e) {
            LOGGER.error("removeHmilyParticipantUndo occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyParticipantUndoByData(final Date date) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_PARTICIPANT_UNDO;
        return removeByFilter(path, HmilyParticipantUndo.class, (undo, params) -> {
            Date dateParam = (Date) params[0];
            return dateParam.before(undo.getUpdateTime()) && undo.getStatus().compareTo(4) == 0;
        }, date);
    }

    @Override
    public int updateHmilyParticipantUndoStatus(final Long undoId, final Integer status) {
        String path = rootPathPrefix + "/" + appName + "/" + HMILY_PARTICIPANT_UNDO + "/" + undoId;
        try {
            if (!checkAndInitPath(path, false)) {
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
        } catch (KeeperException e) {
            LOGGER.error("updateHmilyParticipantStatus occur a exception", e);
        } catch (InterruptedException e) {
            LOGGER.error("updateHmilyParticipantStatus occur a exception", e);
        }

        return HmilyRepository.FAIL_ROWS;
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

    private boolean checkAndInitPath(final String path, final boolean needCreate) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, false);
        if (stat != null) {
            return true;
        }
        if (needCreate) {
            create(path);
        }
        return needCreate;
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
            if (!checkAndInitPath(path, false)) {
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
        } catch (KeeperException e) {
            LOGGER.error("listByFilter occur a exception", e);
        } catch (InterruptedException e) {
            LOGGER.error("listByFilter occur a exception", e);
        }
        return Collections.emptyList();
    }

    private <T> boolean existByFilter(final String path, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            if (!checkAndInitPath(path, false)) {
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
        } catch (KeeperException e) {
            LOGGER.error("existByFilter occur a exception", e);
        } catch (InterruptedException e) {
            LOGGER.error("existByFilter occur a exception", e);
        }
        return false;
    }

    private <T> int removeByFilter(final String path, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            if (checkAndInitPath(path, false)) {
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
        } catch (KeeperException e) {
            LOGGER.error("removeByFilter occur a exception", e);
        } catch (InterruptedException e) {
            LOGGER.error("removeByFilter occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    class PathTokenizer {
        private String path = "";

        private String[] nodes;

        private int index;

        PathTokenizer(final String path) {
            if (path == null) {
                throw new IllegalArgumentException("path cannot be null.");
            }
            nodes = path.split("/");
            if (path.startsWith("/")) {
                index = 1;
            }
        }

        public String nextPath() {
            path = path + "/" + nodes[index];
            index++;
            return path;
        }

        public boolean hasNext() {
            return index < nodes.length;
        }
    }

    interface Filter<T> {
        boolean filter(T t, Object... params);
    }
}
