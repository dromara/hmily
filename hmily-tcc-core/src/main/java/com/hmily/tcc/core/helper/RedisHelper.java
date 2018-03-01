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
package com.hmily.tcc.core.helper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author xiaoyu
 */
public class RedisHelper {

    public static byte[] getRedisKey(String keyPrefix, String id) {
        byte[] prefix = keyPrefix.getBytes();
        final byte[] idBytes = id.getBytes();
        byte[] key = new byte[prefix.length + idBytes.length];
        System.arraycopy(prefix, 0, key, 0, prefix.length);
        System.arraycopy(idBytes, 0, key, prefix.length, idBytes.length);
        return key;
    }

    public static byte[] getKeyValue(JedisPool jedisPool, final byte[] key) {
        return execute(jedisPool, jedis -> {
                    Map<byte[], byte[]> fieldValueMap = jedis.hgetAll(key);
                    List<Map.Entry<byte[], byte[]>> entries = new ArrayList<>(fieldValueMap.entrySet());
                    entries.sort((entry1, entry2) -> (int) (ByteUtils.bytesToLong(entry1.getKey()) - ByteUtils.bytesToLong(entry2.getKey())));
                    if (entries.isEmpty()) {
                        return null;
                    }
                    return entries.get(entries.size() - 1).getValue();
                }
        );
    }

    public static byte[] getKeyValue(Jedis jedis, final byte[] key) {
        Map<byte[], byte[]> fieldValueMap = jedis.hgetAll(key);
        List<Map.Entry<byte[], byte[]>> entries = new ArrayList<Map.Entry<byte[], byte[]>>(fieldValueMap.entrySet());
        entries.sort((entry1, entry2) -> (int) (ByteUtils.bytesToLong(entry1.getKey()) - ByteUtils.bytesToLong(entry2.getKey())));
        if (entries.isEmpty()) {
            return null;
        }
        return entries.get(entries.size() - 1).getValue();
    }

    public static <T> T execute(JedisPool jedisPool, JedisCallback<T> callback) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return callback.doInJedis(jedis);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}