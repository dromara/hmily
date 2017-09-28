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
package com.happylifeplat.tcc.core.helper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
                    if (entries.isEmpty())
                        return null;
                    return entries.get(entries.size() - 1).getValue();
                }
        );
    }

    public static byte[] getKeyValue(Jedis jedis, final byte[] key) {
        Map<byte[], byte[]> fieldValueMap = jedis.hgetAll(key);
        List<Map.Entry<byte[], byte[]>> entries = new ArrayList<Map.Entry<byte[], byte[]>>(fieldValueMap.entrySet());
        entries.sort((entry1, entry2) -> (int) (ByteUtils.bytesToLong(entry1.getKey()) - ByteUtils.bytesToLong(entry2.getKey())));
        if (entries.isEmpty())
            return null;
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