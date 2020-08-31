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

package org.dromara.hmily.repository.redis.jedis;

import java.util.Map;
import java.util.Set;
import redis.clients.jedis.JedisCluster;

/**
 * JedisClientCluster.
 *
 * @author xiaoyu(Myth)、dzc
 */
public class JedisClientCluster implements JedisClient {
    
    private JedisCluster jedisCluster;
    
    public JedisClientCluster(final JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }
    
    @Override
    public String set(final String key, final String value) {
        return jedisCluster.set(key, value);
    }
    
    @Override
    public String set(final String key, final byte[] value) {
        return jedisCluster.set(key.getBytes(), value);
    }
    
    @Override
    public Long del(final String... keys) {
        return jedisCluster.del(keys);
    }
    
    @Override
    public String get(final String key) {
        return jedisCluster.get(key);
    }
    
    @Override
    public byte[] get(final byte[] key) {
        return jedisCluster.get(key);
    }
    
    @Override
    public Set<byte[]> keys(final byte[] pattern) {
        return jedisCluster.hkeys(pattern);
    }
    
    @Override
    public Set<String> keys(final String key) {
        return jedisCluster.hkeys(key);
    }
    
    @Override
    public Long hset(final String key, final String item, final String value) {
        return jedisCluster.hset(key, item, value);
    }
    
    @Override
    public Long hset(final byte[] key, final byte[] field, final byte[] value) {
        return jedisCluster.hset(key, field, value);
    }
    
    @Override
    public String hget(final String key, final String item) {
        return jedisCluster.hget(key, item);
    }
    
    @Override
    public byte[] hget(final byte[] key, final byte[] field) {
        return jedisCluster.hget(key, field);
    }
    
    @Override
    public Long hdel(final String key, final String item) {
        return jedisCluster.hdel(key, item);
    }
    
    @Override
    public Long incr(final String key) {
        return jedisCluster.incr(key);
    }
    
    @Override
    public Long decr(final String key) {
        return jedisCluster.decr(key);
    }
    
    @Override
    public Long expire(final String key, final int second) {
        return jedisCluster.expire(key, second);
    }
    
    @Override
    public Set<String> zrange(final String key, final long start, final long end) {
        return jedisCluster.zrange(key, start, end);
    }
    
    @Override
    public Map<byte[], byte[]> hgetAll(final byte[] key) {
        return jedisCluster.hgetAll(key);
    }
    
    @Override
    public boolean hexists(final byte[] key, final byte[] field) {
        return jedisCluster.hexists(key, field);
    }
}
