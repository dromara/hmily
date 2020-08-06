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

package org.dromara.hmily.core.holder;

import java.util.Objects;
import java.util.Optional;
import org.dromara.hmily.core.cache.HmilyParticipantCacheManager;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;

/**
 * The type Hmily transaction holder.
 */
public final class HmilyTransactionHolder {
    
    private static final HmilyTransactionHolder INSTANCE = new HmilyTransactionHolder();
    
    private static final ThreadLocal<HmilyTransaction> CURRENT = new ThreadLocal<>();
    
    private HmilyTransactionHolder() {
    }
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static HmilyTransactionHolder getInstance() {
        return INSTANCE;
    }
    
    /**
     * Set.
     *
     * @param hmilyTransaction the hmily transaction
     */
    public void set(final HmilyTransaction hmilyTransaction) {
        CURRENT.set(hmilyTransaction);
    }
    
    /**
     * add participant.
     *
     * @param hmilyParticipant {@linkplain HmilyParticipant}
     */
    public void registerStarterParticipant(final HmilyParticipant hmilyParticipant) {
        if (Objects.isNull(hmilyParticipant)) {
            return;
        }
        Optional.ofNullable(getCurrentTransaction())
                .ifPresent(c -> c.registerParticipant(hmilyParticipant));
    }
    
    /**
     * Cache hmily participant.
     *
     * @param hmilyParticipant the hmily participant
     */
    public void cacheHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        if (Objects.isNull(hmilyParticipant)) {
            return;
        }
        HmilyParticipantCacheManager.getInstance().cacheHmilyParticipant(hmilyParticipant);
    }
    
    /**
     * when nested transaction add participant.
     *
     * @param participantId    key
     * @param hmilyParticipant {@linkplain HmilyParticipant}
     */
    public void registerParticipantByNested(final Long participantId, final HmilyParticipant hmilyParticipant) {
        if (Objects.isNull(hmilyParticipant)) {
            return;
        }
        HmilyParticipantCacheManager.getInstance().cacheHmilyParticipant(participantId, hmilyParticipant);
    }
    
    /**
     * acquired by threadLocal.
     *
     * @return {@linkplain HmilyTransaction}
     */
    public HmilyTransaction getCurrentTransaction() {
        return CURRENT.get();
    }
    
    /**
     * clean threadLocal help gc.
     */
    public void remove() {
        CURRENT.remove();
    }
}
