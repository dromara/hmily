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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.hmily.tcc.common.bean.adapter.CoordinatorRepositoryAdapter;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.config.TccConfig;
import com.hmily.tcc.common.config.TccRedisConfig;
import com.hmily.tcc.common.enums.RepositorySupportEnum;
import com.hmily.tcc.common.exception.TccException;
import com.hmily.tcc.common.exception.TccRuntimeException;
import com.hmily.tcc.common.jedis.JedisClient;
import com.hmily.tcc.common.jedis.JedisClientCluster;
import com.hmily.tcc.common.jedis.JedisClientSingle;
import com.hmily.tcc.common.serializer.ObjectSerializer;
import com.hmily.tcc.common.utils.LogUtil;
import com.hmily.tcc.common.utils.RepositoryConvertUtils;
import com.hmily.tcc.common.utils.RepositoryPathUtils;
import com.hmily.tcc.core.spi.CoordinatorRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * redis impl.
 * @author xiaoyu
 */
public class RedisCoordinatorRepository implements CoordinatorRepository {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCoordinatorRepository.class);

    private ObjectSerializer objectSerializer;

    private JedisClient jedisClient;

    private String keyPrefix;

    @Override
    public int create(final TccTransaction tccTransaction) {
        try {
            final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, tccTransaction.getTransId());
            jedisClient.set(redisKey, RepositoryConvertUtils.convert(tccTransaction, objectSerializer));
            return ROWS;
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    @Override
    public int remove(final String id) {
        try {
            final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, id);
            return jedisClient.del(redisKey).intValue();
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    @Override
    public int update(final TccTransaction tccTransaction) throws TccRuntimeException {
        try {
            final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, tccTransaction.getTransId());
            tccTransaction.setVersion(tccTransaction.getVersion() + 1);
            tccTransaction.setLastTime(new Date());
            tccTransaction.setRetriedCount(tccTransaction.getRetriedCount() + 1);
            jedisClient.set(redisKey, RepositoryConvertUtils.convert(tccTransaction, objectSerializer));
            return ROWS;
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    @Override
    public int updateParticipant(final TccTransaction tccTransaction) {
        final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, tccTransaction.getTransId());
        byte[] contents = jedisClient.get(redisKey.getBytes());
        try {
            CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(contents, CoordinatorRepositoryAdapter.class);
            adapter.setContents(objectSerializer.serialize(tccTransaction.getParticipants()));
            jedisClient.set(redisKey, objectSerializer.serialize(adapter));
        } catch (TccException e) {
            e.printStackTrace();
            return FAIL_ROWS;
        }
        return ROWS;
    }

    @Override
    public int updateStatus(final String id, final Integer status) {
        final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, id);
        byte[] contents = jedisClient.get(redisKey.getBytes());
        try {
            CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(contents, CoordinatorRepositoryAdapter.class);
            adapter.setStatus(status);
            jedisClient.set(redisKey, objectSerializer.serialize(adapter));
        } catch (TccException e) {
            e.printStackTrace();
            return FAIL_ROWS;
        }
        return ROWS;
    }

    @Override
    public TccTransaction findById(final String id) {
        try {
            final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, id);
            byte[] contents = jedisClient.get(redisKey.getBytes());
            return RepositoryConvertUtils.transformBean(contents, objectSerializer);
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    @Override
    public List<TccTransaction> listAll() {
        try {
            List<TccTransaction> transactions = Lists.newArrayList();
            Set<byte[]> keys = jedisClient.keys((keyPrefix + "*").getBytes());
            for (final byte[] key : keys) {
                byte[] contents = jedisClient.get(key);
                if (contents != null) {
                    transactions.add(RepositoryConvertUtils.transformBean(contents, objectSerializer));
                }
            }
            return transactions;
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
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
        keyPrefix = RepositoryPathUtils.buildRedisKeyPrefix(modelName);
        final TccRedisConfig tccRedisConfig = tccConfig.getTccRedisConfig();
        try {
            buildJedisPool(tccRedisConfig);
        } catch (Exception e) {
            LogUtil.error(LOGGER, "redis 初始化异常！请检查配置信息:{}", e::getMessage);
        }
    }

    @Override
    public String getScheme() {
        return RepositorySupportEnum.REDIS.getSupport();
    }

    @Override
    public void setSerializer(final ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

    private void buildJedisPool(final TccRedisConfig tccRedisConfig) {
        LogUtil.debug(LOGGER, () -> "开始构建redis配置信息");
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(tccRedisConfig.getMaxIdle());
        //最小空闲连接数, 默认0
        config.setMinIdle(tccRedisConfig.getMinIdle());
        //最大连接数, 默认8个
        config.setMaxTotal(tccRedisConfig.getMaxTotal());
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        config.setMaxWaitMillis(tccRedisConfig.getMaxWaitMillis());
        //在获取连接的时候检查有效性, 默认false
        config.setTestOnBorrow(tccRedisConfig.getTestOnBorrow());
        //返回一个jedis实例给连接池时，是否检查连接可用性（ping()）
        config.setTestOnReturn(tccRedisConfig.getTestOnReturn());
        //在空闲时检查有效性, 默认false
        config.setTestWhileIdle(tccRedisConfig.getTestWhileIdle());
        //逐出连接的最小空闲时间 默认1800000毫秒(30分钟 )
        config.setMinEvictableIdleTimeMillis(tccRedisConfig.getMinEvictableIdleTimeMillis());
        //对象空闲多久后逐出, 当空闲时间>该值 ，且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)，默认30m
        config.setSoftMinEvictableIdleTimeMillis(tccRedisConfig.getSoftMinEvictableIdleTimeMillis());
        //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        config.setTimeBetweenEvictionRunsMillis(tccRedisConfig.getTimeBetweenEvictionRunsMillis());
        //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        config.setNumTestsPerEvictionRun(tccRedisConfig.getNumTestsPerEvictionRun());

        JedisPool jedisPool;
        //如果是集群模式
        if (tccRedisConfig.getCluster()) {
            LogUtil.info(LOGGER, () -> "构造redis集群模式");
            final String clusterUrl = tccRedisConfig.getClusterUrl();
            final Set<HostAndPort> hostAndPorts =
                    Splitter.on(clusterUrl)
                            .splitToList(";")
                            .stream()
                            .map(HostAndPort::parseString).collect(Collectors.toSet());
            JedisCluster jedisCluster = new JedisCluster(hostAndPorts, config);
            jedisClient = new JedisClientCluster(jedisCluster);
        } else {
            if (StringUtils.isNoneBlank(tccRedisConfig.getPassword())) {
                jedisPool = new JedisPool(config, tccRedisConfig.getHostName(), tccRedisConfig.getPort(), tccRedisConfig.getTimeOut(), tccRedisConfig.getPassword());
            } else {
                jedisPool = new JedisPool(config, tccRedisConfig.getHostName(), tccRedisConfig.getPort(), tccRedisConfig.getTimeOut());
            }
            jedisClient = new JedisClientSingle(jedisPool);
        }
    }

}
