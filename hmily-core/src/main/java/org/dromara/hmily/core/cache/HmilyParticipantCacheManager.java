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
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.core.repository.HmilyRepositoryFacade;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;

/**
 * use google guava cache.
 *
 * @author xiaoyu
 */
public final class HmilyParticipantCacheManager {
    
    private static final int MAX_COUNT = 1000000;
    
    private static final HmilyParticipantCacheManager INSTANCE = new HmilyParticipantCacheManager();
    
    private static final LoadingCache<Long, List<HmilyParticipant>> LOADING_CACHE =
            CacheBuilder.newBuilder().maximumWeight(MAX_COUNT)
                    .weigher((Weigher<Long, List<HmilyParticipant>>) (Long, hmilyParticipantList) -> getSize())
                    .build(new CacheLoader<Long, List<HmilyParticipant>>() {
                        @Override
                        public List<HmilyParticipant> load(final Long key) {
                            return cacheHmilyParticipant(key);
                        }
                    });
    
    private HmilyParticipantCacheManager() {
    }
    
    /**
     * HmilyTransactionCacheManager.
     *
     * @return HmilyTransactionCacheManager instance
     */
    public static HmilyParticipantCacheManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Cache hmily participant.
     *
     * @param hmilyParticipant the hmily participant
     */
    public void cacheHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        Long participantId = hmilyParticipant.getParticipantId();
        cacheHmilyParticipant(participantId, hmilyParticipant);
    }
    
    private static List<HmilyParticipant> cacheHmilyParticipant(final Long key) {
        return Optional.ofNullable(HmilyRepositoryFacade.getInstance().findHmilyParticipant(key)).orElse(Collections.emptyList());
    }
    
    /**
     * Cache hmily participant.
     *
     * @param participantId    the participant id
     * @param hmilyParticipant the hmily participant
     */
    public void cacheHmilyParticipant(final Long participantId, final HmilyParticipant hmilyParticipant) {
        List<HmilyParticipant> existHmilyParticipantList = get(participantId);
        if (CollectionUtils.isEmpty(existHmilyParticipantList)) {
            LOADING_CACHE.put(participantId, Lists.newArrayList(hmilyParticipant));
        } else {
            existHmilyParticipantList.add(hmilyParticipant);
            LOADING_CACHE.put(participantId, existHmilyParticipantList);
        }
    }
    
    /**
     * acquire hmilyTransaction.
     *
     * @param participantId this guava key.
     * @return {@linkplain HmilyTransaction}
     */
    public List<HmilyParticipant> get(final Long participantId) {
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
    public void removeByKey(final Long participantId) {
        if (Objects.nonNull(participantId)) {
            LOADING_CACHE.invalidate(participantId);
        }
    }
    
    private static int getSize() {
        return (int) LOADING_CACHE.size();
    }
}
