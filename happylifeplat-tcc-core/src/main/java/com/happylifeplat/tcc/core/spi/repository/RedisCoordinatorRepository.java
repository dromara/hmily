/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.happylifeplat.tcc.core.spi.repository;

import com.google.common.collect.Lists;
import com.happylifeplat.tcc.common.config.TccConfig;
import com.happylifeplat.tcc.common.config.TccRedisConfig;
import com.happylifeplat.tcc.core.bean.entity.TccTransaction;
import com.happylifeplat.tcc.common.enums.RepositorySupportEnum;
import com.happylifeplat.tcc.common.exception.TccException;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;
import com.happylifeplat.tcc.common.utils.LogUtil;
import com.happylifeplat.tcc.core.helper.ByteUtils;
import com.happylifeplat.tcc.core.helper.RedisHelper;
import com.happylifeplat.tcc.core.spi.ObjectSerializer;
import com.happylifeplat.tcc.core.spi.CoordinatorRepository;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xiaoyu
 */
public class RedisCoordinatorRepository implements CoordinatorRepository {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCoordinatorRepository.class);


    private ObjectSerializer objectSerializer;


    private JedisPool jedisPool;

    private String keyName;

    /**
     * 创建本地事务对象
     *
     * @param tccTransaction 事务对象
     * @return rows
     */
    @Override
    public int create(TccTransaction tccTransaction) {
        try {
            final byte[] key = RedisHelper.getRedisKey(keyName, tccTransaction.getTransId());
            Long statusCode = RedisHelper.execute(jedisPool,
                    jedis -> {
                        try {
                            return jedis.hsetnx(key,
                                    ByteUtils.longToBytes(tccTransaction.getVersion()),
                                    objectSerializer.serialize(tccTransaction));
                        } catch (TccException e) {
                            e.printStackTrace();
                            return 0L;
                        }
                    });

            return statusCode.intValue();
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    /**
     * 删除对象
     *
     * @param id 事务对象id
     * @return rows
     */
    @Override
    public int remove(String id) {
        try {
            final byte[] key = RedisHelper.getRedisKey(keyName, id);
            Long result = RedisHelper.execute(jedisPool, jedis -> jedis.del(key));
            return result.intValue();
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    /**
     * 更新数据
     *
     * @param tccTransaction 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     */
    @Override
    public int update(TccTransaction tccTransaction) throws TccRuntimeException {
        try {
            final byte[] key = RedisHelper.getRedisKey(keyName, tccTransaction.getTransId());
            Long statusCode = RedisHelper.execute(jedisPool, jedis -> {
                tccTransaction.setVersion(tccTransaction.getVersion() + 1);
                tccTransaction.setLastTime(new Date());
                tccTransaction.setRetriedCount(tccTransaction.getRetriedCount() + 1);
                try {
                    return jedis.hsetnx(key,
                            ByteUtils.longToBytes(tccTransaction.getVersion()),
                            objectSerializer.serialize(tccTransaction));
                } catch (TccException e) {
                    e.printStackTrace();
                    return 0L;
                }

            });

            final int intValue = statusCode.intValue();
            if (intValue <= 0) {
                throw new TccRuntimeException("数据已经被更新！");
            }
            return intValue;
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }


    /**
     * 根据id获取对象
     *
     * @param id 主键id
     * @return TccTransaction
     */
    @Override
    public TccTransaction findById(String id) {
        try {

            final byte[] key = RedisHelper.getRedisKey(keyName, id);
            byte[] content = RedisHelper.getKeyValue(jedisPool, key);
            if (content != null) {
                return objectSerializer.deSerialize(content, TccTransaction.class);
            }
            return null;
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    /**
     * 获取需要提交的事务
     *
     * @return List<TransactionRecover>
     */
    @Override
    public List<TccTransaction> listAll() {
        try {
            List<TccTransaction> transactions = Lists.newArrayList();
            Set<byte[]> keys = RedisHelper.execute(jedisPool,
                    jedis -> jedis.keys((keyName + "*").getBytes()));
            for (final byte[] key : keys) {
                byte[] content = RedisHelper.getKeyValue(jedisPool, key);
                if (content != null) {
                    transactions.add(objectSerializer.deSerialize(content, TccTransaction.class));
                }
            }
            return transactions;
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<TccTransaction>
     */
    @Override
    public List<TccTransaction> listAllByDelay(Date date) {
        final List<TccTransaction> tccTransactions = listAll();
        return tccTransactions.stream().filter(tccTransaction -> tccTransaction.getLastTime().compareTo(date) > 0).collect(Collectors.toList());
    }

    /**
     * 初始化操作
     *
     * @param modelName 模块名称
     * @param tccConfig 配置信息
     */
    @Override
    public void init(String modelName, TccConfig tccConfig) {
        keyName = modelName;
        final TccRedisConfig tccRedisConfig = tccConfig.getTccRedisConfig();
        try {
            buildJedisPool(tccRedisConfig);
        } catch (Exception e) {
            LogUtil.error(LOGGER, "redis 初始化异常！请检查配置信息:{}", e::getMessage);
        }
    }


    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return RepositorySupportEnum.REDIS.getSupport();
    }

    /**
     * 设置序列化信息
     *
     * @param objectSerializer 序列化实现
     */
    @Override
    public void setSerializer(ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

    private void buildJedisPool(TccRedisConfig tccRedisConfig) {
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
        if (StringUtils.isNoneBlank(tccRedisConfig.getPassword())) {
            jedisPool = new JedisPool(config, tccRedisConfig.getHostName(), tccRedisConfig.getPort(), tccRedisConfig.getTimeOut(), tccRedisConfig.getPassword());
        } else {
            jedisPool = new JedisPool(config, tccRedisConfig.getHostName(), tccRedisConfig.getPort(), tccRedisConfig.getTimeOut());
        }

    }
}
