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

package org.dromara.hmily.core.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.core.coordinator.HmilyCoordinatorService;
import org.dromara.hmily.core.helper.SpringBeanUtils;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * use google guava cache.
 *
 * @author xiaoyu
 */
public final class HmilyTransactionGuavaCacheManager {

    private static final int MAX_COUNT = 1000000;

    private static final LoadingCache<String, HmilyTransaction> LOADING_CACHE =
            CacheBuilder.newBuilder().maximumWeight(MAX_COUNT)
                    .weigher((Weigher<String, HmilyTransaction>) (string, hmilyTransaction) -> getSize())
                    .build(new CacheLoader<String, HmilyTransaction>() {
                        @Override
                        public HmilyTransaction load(final String key) {
                            return cacheHmilyTransaction(key);
                        }
                    });

    private static HmilyCoordinatorService coordinatorService = SpringBeanUtils.getInstance().getBean(HmilyCoordinatorService.class);

    private static final HmilyTransactionGuavaCacheManager TCC_TRANSACTION_CACHE_MANAGER = new HmilyTransactionGuavaCacheManager();

    private HmilyTransactionGuavaCacheManager() {

    }

    /**
     * HmilyTransactionCacheManager.
     *
     * @return HmilyTransactionCacheManager
     */
    public static HmilyTransactionGuavaCacheManager getInstance() {
        return TCC_TRANSACTION_CACHE_MANAGER;
    }

    private static int getSize() {
        return (int) LOADING_CACHE.size();
    }

    private static HmilyTransaction cacheHmilyTransaction(final String key) {
        return Optional.ofNullable(coordinatorService.findByTransId(key)).orElse(new HmilyTransaction());
    }

    /**
     * cache hmilyTransaction.
     *
     * @param hmilyTransaction {@linkplain HmilyTransaction}
     */
    public void cacheHmilyTransaction(final HmilyTransaction hmilyTransaction) {
        LOADING_CACHE.put(hmilyTransaction.getTransId(), hmilyTransaction);
    }

    /**
     * acquire hmilyTransaction.
     *
     * @param key this guava key.
     * @return {@linkplain HmilyTransaction}
     */
    public HmilyTransaction getHmilyTransaction(final String key) {
        try {
            return LOADING_CACHE.get(key);
        } catch (ExecutionException e) {
            return new HmilyTransaction();
        }
    }

    /**
     * remove guava cache by key.
     *
     * @param key guava cache key.
     */
    public void removeByKey(final String key) {
        if (StringUtils.isNoneBlank(key)) {
            LOADING_CACHE.invalidate(key);
        }
    }

}
