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

package org.dromara.hmily.repository.etcd;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Util;
import io.etcd.jetcd.options.GetOption;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.exception.HmilyException;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyEtcdConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.HmilyRepositoryNode;
import org.dromara.hmily.repository.spi.entity.HmilyLock;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.spi.HmilySPI;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * etcd impl.
 *
 * @author dongzl
 */
@HmilySPI("etcd")
@Slf4j
public class EtcdRepository implements HmilyRepository {
    
    private Client client;
    
    private HmilySerializer hmilySerializer;
    
    private HmilyRepositoryNode node;

    private String appName;

    @Override
    public void init(final String appName) {
        this.appName = appName;
        this.node = new HmilyRepositoryNode(appName);
        HmilyEtcdConfig etcdConfig = ConfigEnv.getInstance().getConfig(HmilyEtcdConfig.class);
        client = Client.builder().endpoints(Util.toURIs(Splitter.on(",").trimResults()
                .splitToList(etcdConfig.getHost()))).namespace(ByteSequence.from(etcdConfig.getRootPath(), Charsets.UTF_8)).build();
    }

    @Override
    public void setSerializer(final HmilySerializer hmilySerializer) {
        this.hmilySerializer = hmilySerializer;
    }

    @Override
    public int createHmilyTransaction(final HmilyTransaction hmilyTransaction) throws HmilyRepositoryException {
        String path = node.getHmilyTransactionRealPath(hmilyTransaction.getTransId());
        try {
            boolean exist = isExist(path);
            hmilyTransaction.setAppName(appName);
            if (!exist) {
                hmilyTransaction.setRetry(0);
                hmilyTransaction.setVersion(0);
                hmilyTransaction.setCreateTime(new Date());
            } else {
                hmilyTransaction.setVersion(hmilyTransaction.getVersion() + 1);
            }
            hmilyTransaction.setUpdateTime(new Date());
            client.getKVClient().put(ByteSequence.from(path, StandardCharsets.UTF_8),
                    ByteSequence.from(hmilySerializer.serialize(hmilyTransaction)));
            return HmilyRepository.ROWS;
        } catch (ExecutionException | InterruptedException e) {
            throw new HmilyException(e);
        }
    }

    @Override
    public int updateRetryByLock(final HmilyTransaction hmilyTransaction) {
        final int currentVersion = hmilyTransaction.getVersion();
        String path = node.getHmilyTransactionRealPath(hmilyTransaction.getTransId());
        try {
            KeyValue keyValue = getKeyValue(path);
            if (null == keyValue) {
                log.warn("path {} is not exists.", path);
                return HmilyRepository.FAIL_ROWS;
            }
            if (currentVersion != keyValue.getVersion()) {
                log.warn("current transaction data version different from etcd server. "
                        + "current version: {}, server data version:  {}", currentVersion, keyValue.getVersion());
            }
            hmilyTransaction.setVersion(currentVersion + 1);
            hmilyTransaction.setRetry(hmilyTransaction.getRetry() + 1);
            hmilyTransaction.setUpdateTime(new Date());
            client.getKVClient().put(ByteSequence.from(path, StandardCharsets.UTF_8), ByteSequence.from(hmilySerializer.serialize(hmilyTransaction)));
            return HmilyRepository.ROWS;
        } catch (ExecutionException | InterruptedException e) {
            log.error("updateRetryByLock occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }
    
    private KeyValue getKeyValue(final String path) throws InterruptedException, ExecutionException {
        List<KeyValue> keyValues = client.getKVClient().get(ByteSequence.from(path, StandardCharsets.UTF_8)).get().getKvs();
        return keyValues.isEmpty() ? null : keyValues.iterator().next();
    }
    
    private boolean isExist(final String path) throws InterruptedException, ExecutionException {
        return client.getKVClient().get(ByteSequence.from(path, StandardCharsets.UTF_8))
                .get().getCount() > 0;
    }

    @Override
    public HmilyTransaction findByTransId(final Long transId) {
        String path = node.getHmilyTransactionRealPath(transId);
        try {
            KeyValue keyValue = getKeyValue(path);
            if (keyValue == null) {
                return null;
            }
            return hmilySerializer.deSerialize(keyValue.getValue().getBytes(), HmilyTransaction.class);
        } catch (ExecutionException | InterruptedException e) {
            log.error("findByTransId occur a exception", e);
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
        try {
            KeyValue keyValue = getKeyValue(path);
            if (null == keyValue) {
                return HmilyRepository.FAIL_ROWS;
            }
            HmilyTransaction hmilyTransaction = hmilySerializer.deSerialize(keyValue.getValue().getBytes(), HmilyTransaction.class);
            hmilyTransaction.setStatus(status);
            hmilyTransaction.setVersion(hmilyTransaction.getVersion() + 1);
            hmilyTransaction.setUpdateTime(new Date());
            client.getKVClient().put(ByteSequence.from(path, StandardCharsets.UTF_8), ByteSequence.from(hmilySerializer.serialize(hmilyTransaction)));
            return HmilyRepository.ROWS;
        } catch (ExecutionException | InterruptedException e) {
            log.error("updateHmilyTransactionStatus occur a exception", e);
            return HmilyRepository.FAIL_ROWS;
        }
    }

    @Override
    public int removeHmilyTransaction(final Long transId) {
        String path = node.getHmilyTransactionRealPath(transId);
        try {
            client.getKVClient().delete(ByteSequence.from(path, StandardCharsets.UTF_8)).get();
            return HmilyRepository.ROWS;
        } catch (InterruptedException | ExecutionException e) {
            log.error("removeHmilyTransaction occur a exception", e);
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
            KeyValue keyValue = getKeyValue(path);
            hmilyParticipant.setAppName(appName);
            if (null == keyValue) {
                hmilyParticipant.setRetry(0);
                hmilyParticipant.setVersion(0);
                hmilyParticipant.setCreateTime(new Date());
            } else {
                hmilyParticipant.setVersion(hmilyParticipant.getVersion() + 1);
            }
            hmilyParticipant.setUpdateTime(new Date());
            client.getKVClient().put(ByteSequence.from(path, StandardCharsets.UTF_8), 
                    ByteSequence.from(hmilySerializer.serialize(hmilyParticipant)));
            return HmilyRepository.ROWS;
        } catch (ExecutionException | InterruptedException e) {
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
            KeyValue keyValue = getKeyValue(path);
            if (null == keyValue) {
                return HmilyRepository.FAIL_ROWS;
            }
            HmilyParticipant hmilyParticipant = hmilySerializer.deSerialize(keyValue.getValue().getBytes(), HmilyParticipant.class);
            hmilyParticipant.setStatus(status);
            hmilyParticipant.setVersion(hmilyParticipant.getVersion() + 1);
            hmilyParticipant.setUpdateTime(new Date());
            client.getKVClient().put(ByteSequence.from(path, StandardCharsets.UTF_8), ByteSequence.from(hmilySerializer.serialize(hmilyParticipant)));
            return HmilyRepository.ROWS;
        } catch (ExecutionException | InterruptedException e) {
            log.error("updateHmilyParticipantStatus occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyParticipant(final Long participantId) {
        String path = node.getHmilyParticipantRealPath(participantId);
        try {
            client.getKVClient().delete(ByteSequence.from(path, StandardCharsets.UTF_8)).get();
            return HmilyRepository.ROWS;
        } catch (InterruptedException | ExecutionException e) {
            log.error("removeHmilyParticipant occur a exception", e);
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
            KeyValue keyValue = getKeyValue(path);
            if (null == keyValue) {
                log.warn("path {} is not exists.", path);
                return false;
            }
            if (currentVersion != keyValue.getVersion()) {
                log.warn("current transaction participant data version different from etcd server. "
                        + "current version: {}, server data version:  {}", currentVersion, keyValue.getVersion());
            }
            hmilyParticipant.setVersion(currentVersion + 1);
            hmilyParticipant.setRetry(hmilyParticipant.getRetry() + 1);
            hmilyParticipant.setUpdateTime(new Date());
            client.getKVClient().put(ByteSequence.from(path, StandardCharsets.UTF_8), ByteSequence.from(hmilySerializer.serialize(hmilyParticipant)));
            return true;
        } catch (ExecutionException | InterruptedException e) {
            log.error("updateRetryByLock occur a exception", e);
        }
        return false;
    }

    @Override
    public int createHmilyParticipantUndo(final HmilyParticipantUndo hmilyParticipantUndo) {
        String path = node.getHmilyParticipantUndoRealPath(hmilyParticipantUndo.getUndoId());
        try {
            KeyValue keyValue = getKeyValue(path);
            if (null == keyValue) {
                hmilyParticipantUndo.setCreateTime(new Date());
            }
            hmilyParticipantUndo.setUpdateTime(new Date());
            client.getKVClient().put(ByteSequence.from(path, StandardCharsets.UTF_8), ByteSequence.from(hmilySerializer.serialize(hmilyParticipantUndo)));
            return HmilyRepository.ROWS;
        } catch (ExecutionException | InterruptedException e) {
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
            client.getKVClient().delete(ByteSequence.from(path, StandardCharsets.UTF_8)).get();
            return HmilyRepository.ROWS;
        } catch (InterruptedException | ExecutionException e) {
            log.error("removeHmilyParticipantUndo occur a exception", e);
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
            KeyValue keyValue = getKeyValue(path);
            if (null == keyValue) {
                return HmilyRepository.FAIL_ROWS;
            }
            HmilyParticipantUndo hmilyParticipantUndo = hmilySerializer.deSerialize(keyValue.getValue().getBytes(), HmilyParticipantUndo.class);
            hmilyParticipantUndo.setStatus(status);
            hmilyParticipantUndo.setUpdateTime(new Date());
            client.getKVClient().put(ByteSequence.from(path, StandardCharsets.UTF_8), ByteSequence.from(hmilySerializer.serialize(hmilyParticipantUndo)));
            return HmilyRepository.ROWS;
        } catch (ExecutionException | InterruptedException e) {
            log.error("updateHmilyParticipantStatus occur a exception", e);
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
    
    private <T> List<T> listByFilter(final String path, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            GetOption option = GetOption.newBuilder().withPrefix(ByteSequence.from(path, StandardCharsets.UTF_8)).build();
            List<KeyValue> children = client.getKVClient().get(ByteSequence.from(path, StandardCharsets.UTF_8), option).get().getKvs();
            if (CollectionUtils.isEmpty(children)) {
                return Collections.emptyList();
            }
            List<T> result = new ArrayList<>();
            for (KeyValue child : children) {
                T t = hmilySerializer.deSerialize(child.getValue().getBytes(), deserializeClass);
                if (filter.filter(t, params)) {
                    result.add(t);
                }
            }
            return result;
        } catch (ExecutionException | InterruptedException e) {
            log.error("listByFilter occur a exception", e);
        }
        return Collections.emptyList();
    }

    private <T> boolean existByFilter(final String path, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            GetOption option = GetOption.newBuilder().withPrefix(ByteSequence.from(path, StandardCharsets.UTF_8)).build();
            List<KeyValue> children = client.getKVClient().get(ByteSequence.from(path, StandardCharsets.UTF_8), option).get().getKvs();
            if (CollectionUtils.isEmpty(children)) {
                return false;
            }
            for (KeyValue child : children) {
                T t = hmilySerializer.deSerialize(child.getValue().getBytes(), deserializeClass);
                if (filter.filter(t, params)) {
                    return true;
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error("existByFilter occur a exception", e);
        }
        return false;
    }

    private <T> int removeByFilter(final String path, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            GetOption option = GetOption.newBuilder().withPrefix(ByteSequence.from(path, StandardCharsets.UTF_8)).build();
            List<KeyValue> children = client.getKVClient().get(ByteSequence.from(path, StandardCharsets.UTF_8), option).get().getKvs();
            if (CollectionUtils.isEmpty(children)) {
                return HmilyRepository.FAIL_ROWS;
            }
            int count = 0;
            for (KeyValue child : children) {
                T t = hmilySerializer.deSerialize(child.getValue().getBytes(), deserializeClass);
                if (filter.filter(t, params)) {
                    client.getKVClient().delete(child.getKey());
                    count++;
                }
            }
            return count;
        } catch (ExecutionException | InterruptedException e) {
            log.error("removeByFilter occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
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
