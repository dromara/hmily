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

import java.util.Set;

/**
 * JedisClient.
 * @author xiaoyu(Myth)
 */
public interface JedisClient {

    /**
     * set 操作.
     * @param key key
     * @param value key
     * @return String
     */
    String set(String key, String value);

    /**
     * set 操作.
     * @param key key
     * @param value key
     * @return String
     */
    String set(String key, byte[] value);

    /**
     * 批量删除key.
     * @param keys key集合
     * @return 数量
     */
    Long del(String... keys);

    /**
     * 根据key获取.
     * @param key redis key
     * @return String
     */
    String get(String key);

    /**
     * 根据key获取.
     * @param key redis key
     * @return  byte[]
     */
    byte[] get(byte[] key);


    /**
     * 根据key 模糊匹配.
     * @param pattern redis key
     * @return  Set byte[]
     */
    Set<byte[]> keys(byte[] pattern);

    /**
     * 根据key 模糊匹配.
     * @param key redis key
     * @return      Set String
     */
    Set<String> keys(String key);

    /**
     * hash set值.
     * @param key redis key
     * @param item hash key
     * @param value 值
     * @return 条数
     */
    Long hset(String key, String item, String value);

    /**
     * hash get 值.
     * @param key key
     * @param item hash key
     * @return value
     */
    String hget(String key, String item);

    /**
     * hash del 值.
     * @param key key
     * @param item hash key
     * @return 数量
     */
    Long hdel(String key, String item);

    /**
     * 增加.
     * @param key key
     * @return Long
     */
    Long incr(String key);

    /**
     * 减少.
     * @param key key
     * @return Long
     */
    Long decr(String key);


    /**
     * 设置key的过期时间.
     * @param key key
     * @param second 过期时间 秒
     * @return  Long
     */
    Long expire(String key, int second);

    /**
     * 分页获取zsort.
     * @param key key
     * @param start 开始
     * @param end 结束
     * @return Set String
     */
    Set<String> zrange(String key, long start, long end);

}
