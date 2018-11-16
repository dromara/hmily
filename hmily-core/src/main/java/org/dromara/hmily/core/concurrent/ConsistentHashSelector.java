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

package org.dromara.hmily.core.concurrent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Thread routing selector.
 *
 * @author chenbin sixh
 */
public final class ConsistentHashSelector {

    /**
     * The Replica number.
     */
    private static final int REPLICA_NUMBER = 160;

    private static final int FOUR = 4;

    /**
     * The Virtual invokers.
     */
    private final TreeMap<Long, SingletonExecutor> virtualInvokers;

    /**
     * Instantiates a new Consistent hash selector.
     *
     * @param selects the selects
     */
    public ConsistentHashSelector(final List<SingletonExecutor> selects) {
        this.virtualInvokers = new TreeMap<>();
        for (SingletonExecutor executor : selects) {
            for (int i = 0; i < REPLICA_NUMBER / FOUR; i++) {
                byte[] digest = md5(executor.getName() + i);
                for (int h = 0; h < FOUR; h++) {
                    long m = hash(digest, h);
                    virtualInvokers.put(m, executor);
                }
            }
        }
    }

    /**
     * Select singleton executor.
     *
     * @param key the key
     * @return the singleton executor
     */
    public SingletonExecutor select(final String key) {
        byte[] digest = md5(key);
        return selectForKey(hash(digest, 0));
    }


    /**
     * Select for key singleton executor.
     *
     * @param hash the hash
     * @return the singleton executor
     */
    private SingletonExecutor selectForKey(final long hash) {
        SingletonExecutor invoker;
        Long key = hash;
        if (!virtualInvokers.containsKey(key)) {
            SortedMap<Long, SingletonExecutor> tailMap = virtualInvokers.tailMap(key);
            if (tailMap.isEmpty()) {
                key = virtualInvokers.firstKey();
            } else {
                key = tailMap.firstKey();
            }
        }
        invoker = virtualInvokers.get(key);
        return invoker;
    }

    /**
     * Ketama is a hash algorithm.
     *
     * @param digest digest;
     * @param number numerical;
     * @return hash value ;
     */
    private long hash(final byte[] digest, final int number) {
        return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                | (digest[number * 4] & 0xFF))
                & 0xFFFFFFFFL;
    }

    /**
     * Md 5 byte [ ].
     *
     * @param value the value
     * @return the byte [ ]
     */
    private byte[] md5(final String value) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        md5.reset();
        byte[] bytes;
        bytes = value.getBytes(StandardCharsets.UTF_8);
        md5.update(bytes);
        return md5.digest();
    }

}
