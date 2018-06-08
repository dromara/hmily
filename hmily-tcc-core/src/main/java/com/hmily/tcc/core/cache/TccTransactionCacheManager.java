/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.hmily.tcc.core.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.core.coordinator.CoordinatorService;
import com.hmily.tcc.core.helper.SpringBeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * use google guava cache.
 * @author xiaoyu
 */
public final class TccTransactionCacheManager {

    private static final int MAX_COUNT = 10000;

    private static final LoadingCache<String, TccTransaction> LOADING_CACHE =
            CacheBuilder.newBuilder().maximumWeight(MAX_COUNT)
                    .weigher((Weigher<String, TccTransaction>) (string, tccTransaction) -> getSize())
                    .build(new CacheLoader<String, TccTransaction>() {
                        @Override
                        public TccTransaction load(final String key) {
                            return cacheTccTransaction(key);
                        }
                    });

    private static CoordinatorService coordinatorService = SpringBeanUtils.getInstance().getBean(CoordinatorService.class);

    private static final TccTransactionCacheManager TCC_TRANSACTION_CACHE_MANAGER = new TccTransactionCacheManager();

    private TccTransactionCacheManager() {

    }

    /**
     * TccTransactionCacheManager.
     *
     * @return TccTransactionCacheManager
     */
    public static TccTransactionCacheManager getInstance() {
        return TCC_TRANSACTION_CACHE_MANAGER;
    }

    private static int getSize() {
        return (int) LOADING_CACHE.size();
    }

    private static TccTransaction cacheTccTransaction(final String key) {
        return Optional.ofNullable(coordinatorService.findByTransId(key)).orElse(new TccTransaction());
    }

    /**
     * cache tccTransaction.
     *
     * @param tccTransaction {@linkplain TccTransaction}
     */
    public void cacheTccTransaction(final TccTransaction tccTransaction) {
        LOADING_CACHE.put(tccTransaction.getTransId(), tccTransaction);
    }

    /**
     * acquire TccTransaction.
     *
     * @param key this guava key.
     * @return {@linkplain TccTransaction}
     */
    public TccTransaction getTccTransaction(final String key) {
        try {
            return LOADING_CACHE.get(key);
        } catch (ExecutionException e) {
            return new TccTransaction();
        }
    }

    /**
     * remove guava cache by key.
     * @param key guava cache key.
     */
    public void removeByKey(final String key) {
        if (StringUtils.isNotEmpty(key)) {
            LOADING_CACHE.invalidate(key);
        }
    }

}
