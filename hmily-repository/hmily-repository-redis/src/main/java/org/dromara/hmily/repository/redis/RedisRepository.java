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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyRedisConfig;
import org.dromara.hmily.repository.redis.jedis.JedisClient;
import org.dromara.hmily.repository.redis.jedis.JedisClientCluster;
import org.dromara.hmily.repository.redis.jedis.JedisClientSentinel;
import org.dromara.hmily.repository.redis.jedis.JedisClientSingle;
import org.dromara.hmily.repository.spi.HmilyRepository;
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

/**
 * redis impl.
 *
 * @author xiaoyu
 */
@HmilySPI("redis")
public class RedisRepository implements HmilyRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRepository.class);
    
    private HmilySerializer hmilySerializer;
    
    private JedisClient jedisClient;
    
    private String keyPrefix;
    
    private String appName;
    
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
        return 0;
    }
    
    @Override
    public int updateRetryByLock(final HmilyTransaction hmilyTransaction) {
        return 0;
    }
    
    @Override
    public HmilyTransaction findByTransId(final Long transId) {
        return null;
    }
    
    @Override
    public List<HmilyTransaction> listLimitByDelay(final Date date, final int limit) {
        return null;
    }
    
    @Override
    public int updateHmilyTransactionStatus(final Long transId, final Integer status) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public int removeHmilyTransaction(final Long transId) {
        return 0;
    }
    
    @Override
    public int removeHmilyTransactionByData(final Date date) {
        return 0;
    }
    
    @Override
    public int createHmilyParticipant(final HmilyParticipant hmilyParticipant) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public List<HmilyParticipant> findHmilyParticipant(final Long participantId) {
        return null;
    }
    
    @Override
    public List<HmilyParticipant> listHmilyParticipant(final Date date, final String transType, final int limit) {
        return null;
    }
    
    @Override
    public List<HmilyParticipant> listHmilyParticipantByTransId(final Long transId) {
        return null;
    }
    
    @Override
    public boolean existHmilyParticipantByTransId(final Long transId) {
        return false;
    }
    
    @Override
    public int updateHmilyParticipantStatus(final Long participantId, final Integer status) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public int removeHmilyParticipant(final Long participantId) {
        return 0;
    }
    
    @Override
    public int removeHmilyParticipantByData(final Date date) {
        return 0;
    }
    
    @Override
    public boolean lockHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        return false;
    }
    
    @Override
    public int createHmilyParticipantUndo(final HmilyParticipantUndo hmilyParticipantUndo) {
        return 0;
    }
    
    @Override
    public List<HmilyParticipantUndo> findHmilyParticipantUndoByParticipantId(final Long participantId) {
        return null;
    }
    
    @Override
    public int removeHmilyParticipantUndo(final Long undoId) {
        return 0;
    }
    
    @Override
    public int removeHmilyParticipantUndoByData(final Date date) {
        return 0;
    }
    
    @Override
    public int updateHmilyParticipantUndoStatus(final Long undoId, final Integer status) {
        return 0;
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
    
}
