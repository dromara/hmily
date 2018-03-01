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

package com.hmily.tcc.common.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/26 14:50
 * @since JDK 1.8
 */
public class JedisClientSingle implements JedisClient {

    private JedisPool jedisPool;


    public JedisClientSingle(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public String set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.set(key, value);
        }

    }

    @Override
    public String set(String key, byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.set(key.getBytes(), value);
        }

    }

    @Override
    public Long del(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(keys);
        }

    }

    @Override
    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }

    }

    @Override
    public byte[] get(byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    @Override
    public Set<byte[]> keys(byte[] pattern) {

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys(pattern);
        }

    }

    @Override
    public Set<String> keys(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys(key);
        }
    }

    @Override
    public Long hset(String key, String item, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hset(key, item, value);
        }

    }

    @Override
    public String hget(String key, String item) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, item);
        }
    }

    @Override
    public Long hdel(String key, String item) {

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hdel(key, item);
        }

    }

    @Override
    public Long incr(String key) {

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        }

    }

    @Override
    public Long decr(String key) {

        try (Jedis jedis = jedisPool.getResource()) {

            return jedis.decr(key);
        }

    }

    @Override
    public Long expire(String key, int second) {

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expire(key, second);
        }

    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrange(key, start, end);
        }
    }


}
