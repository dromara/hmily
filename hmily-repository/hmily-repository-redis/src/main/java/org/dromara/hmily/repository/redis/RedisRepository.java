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

package org.dromara.hmily.repository.redis;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.exception.HmilyException;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyRedisConfig;
import org.dromara.hmily.repository.redis.jedis.JedisClient;
import org.dromara.hmily.repository.redis.jedis.JedisClientCluster;
import org.dromara.hmily.repository.redis.jedis.JedisClientSentinel;
import org.dromara.hmily.repository.redis.jedis.JedisClientSingle;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyLock;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.spi.HmilySPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * redis impl.
 *
 * @author dzc
 */
@HmilySPI("redis")
public class RedisRepository implements HmilyRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRepository.class);
    
    private static final String HMILY_TRANSACTION_GLOBAL = "hmily_transaction_global";
    
    private static final String HMILY_TRANSACTION_PARTICIPANT = "hmily_transaction_participant";
    
    private static final String HMILY_PARTICIPANT_UNDO = "hmily_participant_undo";
    
    private String rootPathPrefix = "hmily";
    
    private String keyPrefix = "-";
    
    private String appName;
    
    private HmilySerializer hmilySerializer;
    
    private JedisClient jedisClient;
    
    @Override
    public void init(final String appName) {
        this.appName = appName;
        HmilyRedisConfig hmilyRedisConfig = ConfigEnv.getInstance().getConfig(HmilyRedisConfig.class);
        try {
            buildJedisPool(hmilyRedisConfig);
        } catch (Exception e) {
            LOGGER.error("redis init error please check you config:{}", e.getMessage());
            throw new HmilyRepositoryException(e);
        }
    }
    
    @Override
    public void setSerializer(final HmilySerializer hmilySerializer) {
        this.hmilySerializer = hmilySerializer;
    }
    
    @Override
    public int createHmilyTransaction(final HmilyTransaction hmilyTransaction) throws HmilyRepositoryException {
        try {
            String transId = buildHmilyTransactionRealPath(hmilyTransaction.getTransId());
            final boolean exist = jedisClient.hexists(HMILY_TRANSACTION_GLOBAL.getBytes(), transId.getBytes());
            if (!exist) {
                hmilyTransaction.setRetry(0);
                hmilyTransaction.setVersion(0);
                hmilyTransaction.setCreateTime(new Date());
            } else {
                hmilyTransaction.setVersion(hmilyTransaction.getVersion() + 1);
            }
            hmilyTransaction.setUpdateTime(new Date());
            jedisClient.hset(HMILY_TRANSACTION_GLOBAL.getBytes(), transId.getBytes(), hmilySerializer.serialize(hmilyTransaction));
            return HmilyRepository.ROWS;
        } catch (JedisException e) {
            throw new HmilyException(e);
        }
    }
    
    @Override
    public int updateRetryByLock(final HmilyTransaction hmilyTransaction) {
        try {
            String transId = buildHmilyTransactionRealPath(hmilyTransaction.getTransId());
            final boolean exist = jedisClient.hexists(HMILY_TRANSACTION_GLOBAL.getBytes(), transId.getBytes());
            if (!exist) {
                return HmilyRepository.FAIL_ROWS;
            } else {
                hmilyTransaction.setVersion(hmilyTransaction.getVersion() + 1);
                hmilyTransaction.setUpdateTime(new Date());
                jedisClient.hset(HMILY_TRANSACTION_GLOBAL.getBytes(), transId.getBytes(), hmilySerializer.serialize(hmilyTransaction));
            }
            return HmilyRepository.ROWS;
        } catch (JedisException e) {
            throw new HmilyException(e);
        }
    }
    
    @Override
    public HmilyTransaction findByTransId(final Long transId) {
        try {
            String key = buildHmilyTransactionRealPath(transId);
            final boolean exist = jedisClient.hexists(HMILY_TRANSACTION_GLOBAL.getBytes(), key.getBytes());
            if (!exist) {
                return null;
            } else {
                byte[] data = jedisClient.hget(HMILY_TRANSACTION_GLOBAL.getBytes(), key.getBytes());
                if (data == null) {
                    return null;
                }
                return hmilySerializer.deSerialize(data, HmilyTransaction.class);
            }
        } catch (JedisException e) {
            LOGGER.error("transId occur a exception", e);
            throw new HmilyException(e);
        }
    }
    
    @Override
    public List<HmilyTransaction> listLimitByDelay(final Date date, final int limit) {
        String key = buildHmilyTransactionRootPath();
        return listByFilter(key, HmilyTransaction.class, (hmilyTransaction, params) -> {
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
    
    private <T> List<T> listByFilter(final String key, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            Map<byte[], byte[]> dataAll = jedisClient.hgetAll(key.getBytes());
            if (Objects.isNull(dataAll)) {
                return Collections.emptyList();
            }
            List<T> result = new ArrayList<>();
            for (Entry<byte[], byte[]> entry : dataAll.entrySet()) {
                byte[] data = entry.getValue();
                if (data == null) {
                    continue;
                }
                T t = hmilySerializer.deSerialize(data, deserializeClass);
                if (filter.filter(t, params)) {
                    result.add(t);
                }
            }
            return result;
        } catch (JedisException e) {
            LOGGER.error("listByFilter occur a exception", e);
        }
        return Collections.emptyList();
    }
    
    @Override
    public int updateHmilyTransactionStatus(final Long transId, final Integer status) throws HmilyRepositoryException {
        byte[] key = buildHmilyTransactionRealPath(transId).getBytes();
        try {
            byte[] data = jedisClient.hget(HMILY_TRANSACTION_GLOBAL.getBytes(), key);
            if (data == null) {
                return HmilyRepository.FAIL_ROWS;
            }
            HmilyTransaction hmilyTransaction = hmilySerializer.deSerialize(data, HmilyTransaction.class);
            hmilyTransaction.setStatus(status);
            hmilyTransaction.setVersion(hmilyTransaction.getVersion() + 1);
            hmilyTransaction.setUpdateTime(new Date());
            jedisClient.hset(HMILY_TRANSACTION_GLOBAL.getBytes(), key, hmilySerializer.serialize(hmilyTransaction));
            return HmilyRepository.ROWS;
        } catch (JedisException e) {
            LOGGER.error("updateHmilyTransactionStatus occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }
    
    @Override
    public int removeHmilyTransaction(final Long transId) {
        String key = buildHmilyTransactionRealPath(transId);
        try {
            jedisClient.hdel(HMILY_TRANSACTION_GLOBAL, key);
            return HmilyRepository.ROWS;
        } catch (JedisException e) {
            LOGGER.error("removeHmilyTransaction occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }
    
    @Override
    public int removeHmilyTransactionByDate(final Date date) {
        String key = buildHmilyTransactionRootPath();
        return removeByFilter(key, HmilyTransaction.class, (hmilyTransaction, params) -> {
            Date dateParam = (Date) params[0];
            return dateParam.after(hmilyTransaction.getUpdateTime()) && hmilyTransaction.getStatus() == HmilyActionEnum.DELETE.getCode();
        }, date);
    }
    
    @Override
    public int createHmilyParticipant(final HmilyParticipant hmilyParticipant) throws HmilyRepositoryException {
        try {
            byte[] transId = buildHmilyParticipantRealPath(hmilyParticipant.getTransId()).getBytes();
            boolean exist = jedisClient.hexists(HMILY_TRANSACTION_PARTICIPANT.getBytes(), transId);
            if (!exist) {
                hmilyParticipant.setRetry(0);
                hmilyParticipant.setVersion(0);
                hmilyParticipant.setCreateTime(new Date());
            } else {
                hmilyParticipant.setVersion(hmilyParticipant.getVersion() + 1);
            }
            hmilyParticipant.setUpdateTime(new Date());
            jedisClient.hset(HMILY_TRANSACTION_PARTICIPANT.getBytes(), String.valueOf(hmilyParticipant.getParticipantId()).getBytes(), hmilySerializer.serialize(hmilyParticipant));
            return HmilyRepository.ROWS;
        } catch (JedisException e) {
            throw new HmilyException(e);
        }
    }
    
    @Override
    public List<HmilyParticipant> findHmilyParticipant(final Long participantId) {
        String key = buildHmilyParticipantRootPath();
        return listByFilter(key, HmilyParticipant.class, (hmilyParticipant, params) -> {
            Long participantIdParam = (Long) params[0];
            return participantIdParam.compareTo(hmilyParticipant.getParticipantId()) == 0
                    || (hmilyParticipant.getParticipantRefId() != null && participantIdParam.compareTo(hmilyParticipant.getParticipantRefId()) == 0);
        }, participantId);
    }
    
    @Override
    public List<HmilyParticipant> listHmilyParticipant(final Date date, final String transType, final int limit) {
        String path = buildHmilyParticipantRootPath();
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
        String key = buildHmilyParticipantRootPath();
        return listByFilter(key, HmilyParticipant.class, (hmilyParticipant, params) -> transId.compareTo(hmilyParticipant.getTransId()) == 0, transId);
    }
    
    @Override
    public boolean existHmilyParticipantByTransId(final Long transId) {
        String key = buildHmilyParticipantRootPath();
        return existByFilter(key, HmilyParticipant.class, (hmilyParticipant, params) -> {
            Long transIdParam = (Long) params[0];
            return transIdParam.compareTo(hmilyParticipant.getTransId()) == 0;
        }, transId);
    }
    
    @Override
    public int updateHmilyParticipantStatus(final Long participantId, final Integer status) throws HmilyRepositoryException {
        byte[] key = buildHmilyParticipantRealPath(participantId).getBytes();
        try {
            byte[] data = jedisClient.hget(HMILY_TRANSACTION_PARTICIPANT.getBytes(), key);
            if (data == null) {
                return HmilyRepository.FAIL_ROWS;
            }
            HmilyParticipant hmilyParticipant = hmilySerializer.deSerialize(data, HmilyParticipant.class);
            hmilyParticipant.setStatus(status);
            hmilyParticipant.setVersion(hmilyParticipant.getVersion() + 1);
            hmilyParticipant.setUpdateTime(new Date());
            jedisClient.hset(HMILY_TRANSACTION_PARTICIPANT.getBytes(), String.valueOf(hmilyParticipant.getParticipantId()).getBytes(), hmilySerializer.serialize(hmilyParticipant));
            return HmilyRepository.ROWS;
        } catch (JedisException e) {
            LOGGER.error("updateHmilyParticipantStatus occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }
    
    @Override
    public int removeHmilyParticipant(final Long participantId) {
        String key = buildHmilyParticipantRealPath(participantId);
        try {
            jedisClient.hdel(HMILY_TRANSACTION_PARTICIPANT, key);
            return HmilyRepository.ROWS;
        } catch (JedisException e) {
            LOGGER.error("removeHmilyParticipant occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }
    
    @Override
    public int removeHmilyParticipantByDate(final Date date) {
        String key = buildHmilyParticipantRootPath();
        return removeByFilter(key, HmilyParticipant.class, (hmilyParticipant, params) -> {
            Date dateParam = (Date) params[0];
            return dateParam.after(hmilyParticipant.getUpdateTime()) && hmilyParticipant.getStatus().compareTo(HmilyActionEnum.DELETE.getCode()) == 0;
        }, date);
    }
    
    @Override
    public boolean lockHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        final int currentVersion = hmilyParticipant.getVersion();
        String key = buildHmilyParticipantRealPath(hmilyParticipant.getParticipantId());
        try {
            boolean exist = jedisClient.hexists(HMILY_TRANSACTION_PARTICIPANT.getBytes(), key.getBytes());
            if (!exist) {
                LOGGER.warn("key {} is not exists.", key);
                return false;
            }
            hmilyParticipant.setVersion(currentVersion + 1);
            hmilyParticipant.setRetry(hmilyParticipant.getRetry() + 1);
            hmilyParticipant.setUpdateTime(new Date());
            jedisClient.hset(HMILY_TRANSACTION_PARTICIPANT.getBytes(), String.valueOf(hmilyParticipant.getParticipantId()).getBytes(), hmilySerializer.serialize(hmilyParticipant));
            return true;
        } catch (JedisException e) {
            LOGGER.error("updateRetryByLock occur a exception", e);
        }
        return false;
    }
    
    @Override
    public int createHmilyParticipantUndo(final HmilyParticipantUndo hmilyParticipantUndo) {
        try {
            byte[] transId = buildHmilyParticipantRealPath(hmilyParticipantUndo.getTransId()).getBytes();
            boolean exist = jedisClient.hexists(HMILY_PARTICIPANT_UNDO.getBytes(), transId);
            if (!exist) {
                hmilyParticipantUndo.setCreateTime(new Date());
            }
            hmilyParticipantUndo.setUpdateTime(new Date());
            jedisClient.hset(HMILY_PARTICIPANT_UNDO.getBytes(), String.valueOf(hmilyParticipantUndo.getUndoId()).getBytes(), hmilySerializer.serialize(hmilyParticipantUndo));
            return HmilyRepository.ROWS;
        } catch (JedisException e) {
            throw new HmilyException(e);
        }
    }
    
    @Override
    public List<HmilyParticipantUndo> findHmilyParticipantUndoByParticipantId(final Long participantId) {
        String key = buildHmilyParticipantUndoRootPath();
        return listByFilter(key, HmilyParticipantUndo.class, (undo, params) -> {
            Long participantIdParam = (Long) params[0];
            return participantIdParam.compareTo(undo.getParticipantId()) == 0;
        }, participantId);
    }
    
    @Override
    public int removeHmilyParticipantUndo(final Long undoId) {
        String key = buildHmilyParticipantUndoRealPath(undoId);
        try {
            jedisClient.hdel(HMILY_PARTICIPANT_UNDO, key);
            return HmilyRepository.ROWS;
        } catch (JedisException e) {
            LOGGER.error("removeHmilyParticipantUndo occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }
    
    @Override
    public int removeHmilyParticipantUndoByDate(final Date date) {
        String key = buildHmilyParticipantUndoRootPath();
        return removeByFilter(key, HmilyParticipantUndo.class, (undo, params) -> {
            Date dateParam = (Date) params[0];
            return dateParam.after(undo.getUpdateTime()) && undo.getStatus().compareTo(HmilyActionEnum.DELETE.getCode()) == 0;
        }, date);
    }
    
    @Override
    public int updateHmilyParticipantUndoStatus(final Long undoId, final Integer status) {
        String key = buildHmilyParticipantUndoRealPath(undoId);
        try {
            byte[] data = jedisClient.hget(HMILY_PARTICIPANT_UNDO.getBytes(), key.getBytes());
            if (data == null) {
                return HmilyRepository.FAIL_ROWS;
            }
            
            HmilyParticipantUndo hmilyParticipantUndo = hmilySerializer.deSerialize(data, HmilyParticipantUndo.class);
            hmilyParticipantUndo.setStatus(status);
            hmilyParticipantUndo.setUpdateTime(new Date());
            jedisClient.hset(HMILY_PARTICIPANT_UNDO.getBytes(), String.valueOf(hmilyParticipantUndo.getUndoId()).getBytes(), hmilySerializer.serialize(hmilyParticipantUndo));
            return HmilyRepository.ROWS;
        } catch (JedisException e) {
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
    
    private <T> int removeByFilter(final String key, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            Map<byte[], byte[]> dataAll = jedisClient.hgetAll(key.getBytes());
            if (Objects.isNull(dataAll)) {
                return HmilyRepository.FAIL_ROWS;
            }
            int count = 0;
            for (Entry<byte[], byte[]> entry : dataAll.entrySet()) {
                byte[] data = entry.getValue();
                if (data == null) {
                    continue;
                }
                T t = hmilySerializer.deSerialize(data, deserializeClass);
                if (filter.filter(t, params)) {
                    jedisClient.hdel(rootPathPrefix, key);
                    count++;
                }
                return count;
            }
        } catch (JedisException e) {
            LOGGER.error("removeByFilter occur a exception", e);
        }
        return HmilyRepository.FAIL_ROWS;
    }
    
    private <T> boolean existByFilter(final String key, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            Map<byte[], byte[]> dataAll = jedisClient.hgetAll(key.getBytes());
            if (Objects.isNull(dataAll)) {
                return false;
            }
            for (Entry<byte[], byte[]> entry : dataAll.entrySet()) {
                byte[] data = entry.getValue();
                if (data == null) {
                    continue;
                }
                T t = hmilySerializer.deSerialize(data, deserializeClass);
                if (filter.filter(t, params)) {
                    return true;
                }
            }
        } catch (JedisException e) {
            LOGGER.error("existByFilter occur a exception", e);
        }
        return false;
    }
    
    private String buildHmilyTransactionRootPath() {
        return rootPathPrefix + keyPrefix + HMILY_TRANSACTION_GLOBAL;
    }
    
    private String buildHmilyTransactionRealPath(final Long transId) {
        return buildHmilyTransactionRootPath() + keyPrefix + transId;
    }
    
    private String buildHmilyParticipantRootPath() {
        return rootPathPrefix + keyPrefix + appName + keyPrefix + HMILY_TRANSACTION_PARTICIPANT;
    }
    
    private String buildHmilyParticipantRealPath(final Long participantId) {
        return buildHmilyParticipantRootPath() + keyPrefix + participantId;
    }
    
    private String buildHmilyParticipantUndoRootPath() {
        return rootPathPrefix + keyPrefix + appName + keyPrefix + HMILY_PARTICIPANT_UNDO;
    }
    
    private String buildHmilyParticipantUndoRealPath(final Long undoId) {
        return buildHmilyParticipantUndoRootPath() + "/" + undoId;
    }
    
    private void buildJedisPool(final HmilyRedisConfig hmilyRedisConfig) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(hmilyRedisConfig.getMaxIdle());
        config.setMinIdle(hmilyRedisConfig.getMinIdle());
        config.setMaxTotal(hmilyRedisConfig.getMaxTotal());
        config.setMaxWaitMillis(hmilyRedisConfig.getMaxWaitMillis());
        config.setTestOnBorrow(hmilyRedisConfig.isTestOnBorrow());
        config.setTestOnReturn(hmilyRedisConfig.isTestOnReturn());
        config.setTestWhileIdle(hmilyRedisConfig.isTestWhileIdle());
        config.setMinEvictableIdleTimeMillis(hmilyRedisConfig.getMinEvictableIdleTimeMillis());
        config.setSoftMinEvictableIdleTimeMillis(hmilyRedisConfig.getSoftMinEvictableIdleTimeMillis());
        config.setTimeBetweenEvictionRunsMillis(hmilyRedisConfig.getTimeBetweenEvictionRunsMillis());
        config.setNumTestsPerEvictionRun(hmilyRedisConfig.getNumTestsPerEvictionRun());
        JedisPool jedisPool;
        if (hmilyRedisConfig.isCluster()) {
            LogUtil.info(LOGGER, () -> "build redis cluster ............");
            final String clusterUrl = hmilyRedisConfig.getClusterUrl();
            final Set<HostAndPort> hostAndPorts =
                    Lists.newArrayList(Splitter.on(";")
                            .split(clusterUrl))
                            .stream()
                            .map(HostAndPort::parseString).collect(Collectors.toSet());
            JedisCluster jedisCluster = new JedisCluster(hostAndPorts, config);
            jedisClient = new JedisClientCluster(jedisCluster);
        } else if (hmilyRedisConfig.isSentinel()) {
            LogUtil.info(LOGGER, () -> "build redis sentinel ............");
            final String sentinelUrl = hmilyRedisConfig.getSentinelUrl();
            final Set<String> hostAndPorts =
                    new HashSet<>(Lists.newArrayList(Splitter.on(";").split(sentinelUrl)));
            JedisSentinelPool pool =
                    new JedisSentinelPool(hmilyRedisConfig.getMasterName(), hostAndPorts,
                            config, hmilyRedisConfig.getTimeOut(), hmilyRedisConfig.getPassword());
            jedisClient = new JedisClientSentinel(pool);
        } else {
            if (StringUtils.isNoneBlank(hmilyRedisConfig.getPassword())) {
                jedisPool = new JedisPool(config, hmilyRedisConfig.getHostName(), hmilyRedisConfig.getPort(), hmilyRedisConfig.getTimeOut(), hmilyRedisConfig.getPassword());
            } else {
                jedisPool = new JedisPool(config, hmilyRedisConfig.getHostName(), hmilyRedisConfig.getPort(), hmilyRedisConfig.getTimeOut());
            }
            jedisClient = new JedisClientSingle(jedisPool);
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
