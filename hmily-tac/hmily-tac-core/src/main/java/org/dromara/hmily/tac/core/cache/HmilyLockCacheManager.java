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

package org.dromara.hmily.tac.core.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import org.dromara.hmily.core.repository.HmilyRepositoryFacade;
import org.dromara.hmily.repository.spi.entity.HmilyLock;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Hmily lock cache manager.
 *
 * @author zhaojun
 */
public final class HmilyLockCacheManager {
    
    private static final HmilyLockCacheManager INSTANCE = new HmilyLockCacheManager();
    
    private static final int MAX_COUNT = 1000000;
    
    private final LoadingCache<String, Optional<HmilyLock>> loadingCache =
            CacheBuilder.newBuilder().maximumWeight(MAX_COUNT)
                    .weigher((Weigher<String, Optional<HmilyLock>>) (e1, e2) -> getSize())
                    .build(new CacheLoader<String, Optional<HmilyLock>>() {
                        @Override
                        public Optional<HmilyLock> load(final String key) {
                            return HmilyRepositoryFacade.getInstance().findHmilyLockById(key);
                        }
                    });
    
    private HmilyLockCacheManager() {
    }
    
    /**
     * Hmily lock cache manager.
     *
     * @return Hmily lock cache manager instance
     */
    public static HmilyLockCacheManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Cache hmily lock.
     *
     * @param lockId lock id
     * @param hmilyLock the hmily lock
     */
    public void cacheHmilyLock(final String lockId, final HmilyLock hmilyLock) {
        loadingCache.put(lockId, Optional.of(hmilyLock));
    }
    
    /**
     * Acquire hmily lock.
     *
     * @param lockId this guava key.
     * @return {@linkplain HmilyTransaction}
     */
    public Optional<HmilyLock> get(final String lockId) {
        try {
            return loadingCache.get(lockId);
        } catch (ExecutionException ex) {
            return Optional.empty();
        }
    }
    
    /**
     * remove guava cache by key.
     *
     * @param lockId guava cache key.
     */
    public void removeByKey(final String lockId) {
        if (Objects.nonNull(lockId)) {
            loadingCache.invalidate(lockId);
        }
    }
    
    private int getSize() {
        return (int) loadingCache.size();
    }
}
