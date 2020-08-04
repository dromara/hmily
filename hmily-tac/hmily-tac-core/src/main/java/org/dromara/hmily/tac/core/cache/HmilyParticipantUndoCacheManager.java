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
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.core.repository.HmilyRepositoryFacade;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;

/**
 * use google guava cache.
 *
 * @author xiaoyu
 */
public final class HmilyParticipantUndoCacheManager {
    
    private static final HmilyParticipantUndoCacheManager INSTANCE = new HmilyParticipantUndoCacheManager();
    
    private static final int MAX_COUNT = 1000000;
    
    private final LoadingCache<String, List<HmilyParticipantUndo>> LOADING_CACHE =
            CacheBuilder.newBuilder().maximumWeight(MAX_COUNT)
                    .weigher((Weigher<String, List<HmilyParticipantUndo>>) (string, hmilyParticipantUndoList) -> getSize())
                    .build(new CacheLoader<String, List<HmilyParticipantUndo>>() {
                        @Override
                        public List<HmilyParticipantUndo> load(final String key) {
                            return cacheHmilyParticipantUndo(key);
                        }
                    });
    
    
    private HmilyParticipantUndoCacheManager() {
    }
    
    /**
     * HmilyTransactionCacheManager.
     *
     * @return HmilyTransactionCacheManager
     */
    public static HmilyParticipantUndoCacheManager getInstance() {
        return INSTANCE;
    }
    
    public void cacheHmilyParticipantUndo(final HmilyParticipantUndo hmilyParticipantUndo) {
        String participantId = hmilyParticipantUndo.getParticipantId();
        cacheHmilyParticipantUndo(participantId, hmilyParticipantUndo);
    }
    
    public void cacheHmilyParticipantUndo(final String participantId, final HmilyParticipantUndo hmilyParticipantUndo) {
        List<HmilyParticipantUndo> existList = get(participantId);
        if (CollectionUtils.isEmpty(existList)) {
            LOADING_CACHE.put(participantId, Lists.newArrayList(hmilyParticipantUndo));
        } else {
            existList.add(hmilyParticipantUndo);
            LOADING_CACHE.put(participantId, existList);
        }
    }
    
    /**
     * acquire hmilyTransaction.
     *
     * @param participantId this guava key.
     * @return {@linkplain HmilyTransaction}
     */
    public List<HmilyParticipantUndo> get(final String participantId) {
        try {
            return LOADING_CACHE.get(participantId);
        } catch (ExecutionException e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * remove guava cache by key.
     *
     * @param participantId guava cache key.
     */
    public void removeByKey(final String participantId) {
        if (StringUtils.isNoneBlank(participantId)) {
            LOADING_CACHE.invalidate(participantId);
        }
    }
    
    private int getSize() {
        return (int) LOADING_CACHE.size();
    }
    
    private List<HmilyParticipantUndo> cacheHmilyParticipantUndo(final String participantId) {
        return Optional.ofNullable(HmilyRepositoryFacade.getInstance().findUndoByParticipantId(participantId)).orElse(Collections.emptyList());
    }
}
