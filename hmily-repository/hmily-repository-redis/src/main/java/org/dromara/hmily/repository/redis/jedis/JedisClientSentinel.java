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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

/**
 * JedisClientSentinel.
 *
 * @author xiaoyu(Myth)、dzc
 */
public class JedisClientSentinel implements JedisClient {
    
    private JedisSentinelPool jedisSentinelPool;
    
    public JedisClientSentinel(final JedisSentinelPool jedisSentinelPool) {
        this.jedisSentinelPool = jedisSentinelPool;
    }
    
    @Override
    public String set(final String key, final String value) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.set(key, value);
        }
    }
    
    @Override
    public String set(final String key, final byte[] value) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.set(key.getBytes(), value);
        }
    }
    
    @Override
    public Long del(final String... keys) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.del(keys);
        }
    }
    
    @Override
    public String get(final String key) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.get(key);
        }
    }
    
    @Override
    public byte[] get(final byte[] key) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.get(key);
        }
    }
    
    @Override
    public Set<byte[]> keys(final byte[] pattern) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.keys(pattern);
        }
    }
    
    @Override
    public Set<String> keys(final String key) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.keys(key);
        }
    }
    
    @Override
    public Long hset(final String key, final String item, final String value) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.hset(key, item, value);
        }
    }
    
    @Override
    public Long hset(final byte[] key, final byte[] field, final byte[] value) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.hset(key, field, value);
        }
    }
    
    @Override
    public String hget(final String key, final String item) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.hget(key, item);
        }
    }
    
    @Override
    public byte[] hget(final byte[] key, final byte[] field) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.hget(key, field);
        }
    }
    
    @Override
    public Long hdel(final String key, final String item) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.hdel(key, item);
        }
    }
    
    @Override
    public Long incr(final String key) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.incr(key);
        }
    }
    
    @Override
    public Long decr(final String key) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.decr(key);
        }
    }
    
    @Override
    public Long expire(final String key, final int second) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.expire(key, second);
        }
    }
    
    @Override
    public Set<String> zrange(final String key, final long start, final long end) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.zrange(key, start, end);
        }
    }
    
    @Override
    public Map<byte[], byte[]> hgetAll(final byte[] key) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.hgetAll(key);
        }
    }
    
    @Override
    public boolean hexists(final byte[] key, final byte[] field) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.hexists(key, field);
        }
    }
}
