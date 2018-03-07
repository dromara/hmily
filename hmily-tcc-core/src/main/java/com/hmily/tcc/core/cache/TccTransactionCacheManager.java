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
import com.hmily.tcc.common.exception.TccRuntimeException;
import com.hmily.tcc.core.coordinator.CoordinatorService;
import com.hmily.tcc.core.helper.SpringBeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author xiaoyu
 */
public class TccTransactionCacheManager {

    private static final int MAX_COUNT = 10000;

    private static CoordinatorService coordinatorService
            = SpringBeanUtils.getInstance().getBean(CoordinatorService.class);


    private static final TccTransactionCacheManager TCC_TRANSACTION_CACHE_MANAGER = new TccTransactionCacheManager();

    private TccTransactionCacheManager() {

    }

    public static TccTransactionCacheManager getInstance() {
        return TCC_TRANSACTION_CACHE_MANAGER;
    }


    private static final LoadingCache<String, TccTransaction> LOADING_CACHE = CacheBuilder.newBuilder()
            .maximumWeight(MAX_COUNT)
            .weigher((Weigher<String, TccTransaction>) (string, TccTransaction) -> getSize())
            .build(new CacheLoader<String, TccTransaction>() {
                @Override
                public TccTransaction load(String key) throws Exception {
                    return cacheTccTransaction(key);
                }
            });


    private static int getSize() {
        return (int) LOADING_CACHE.size();
    }


    private static TccTransaction cacheTccTransaction(String key) {
        final TccTransaction tccTransaction = coordinatorService.findByTransId(key);
        if (Objects.isNull(tccTransaction)) {
            return new TccTransaction();
        }
        return tccTransaction;
    }


    /**
     * cache 缓存
     *
     * @param tccTransaction 事务对象
     */
    public void cacheTccTransaction(TccTransaction tccTransaction) {
        LOADING_CACHE.put(tccTransaction.getTransId(), tccTransaction);
    }

    /**
     * 获取task
     *
     * @param key 需要获取的key
     */
    public TccTransaction getTccTransaction(String key) {
        try {
            return LOADING_CACHE.get(key);
        } catch (ExecutionException e) {
            return new TccTransaction();
        }
    }


    public void removeByKey(String key) {
        if (StringUtils.isNotEmpty(key)) {
            LOADING_CACHE.invalidate(key);
        }
    }

}
