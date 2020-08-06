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

package org.dromara.hmily.core.repository;

import java.util.Objects;
import org.dromara.hmily.common.enums.EventTypeEnum;
import org.dromara.hmily.core.disruptor.publisher.HmilyRepositoryEventPublisher;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;

/**
 * The type Hmily repository storage.
 */
public class HmilyRepositoryStorage {
    
    private static final HmilyRepositoryEventPublisher PUBLISHER = HmilyRepositoryEventPublisher.getInstance();
    
    /**
     * Create hmily transaction.
     *
     * @param hmilyTransaction the hmily transaction
     */
    public static void createHmilyTransaction(final HmilyTransaction hmilyTransaction) {
        if (Objects.nonNull(hmilyTransaction)) {
            PUBLISHER.publishEvent(hmilyTransaction, EventTypeEnum.CREATE_HMILY_TRANSACTION.getCode());
        }
    }
    
    /**
     * Update hmily transaction status.
     *
     * @param hmilyTransaction the hmily transaction
     */
    public static void updateHmilyTransactionStatus(final HmilyTransaction hmilyTransaction) {
        if (Objects.nonNull(hmilyTransaction)) {
            PUBLISHER.publishEvent(hmilyTransaction, EventTypeEnum.UPDATE_HMILY_TRANSACTION_STATUS.getCode());
        }
    }
    
    /**
     * Create hmily participant.
     *
     * @param hmilyParticipant the hmily participant
     */
    public static void createHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        if (Objects.nonNull(hmilyParticipant)) {
            PUBLISHER.publishEvent(hmilyParticipant, EventTypeEnum.CREATE_HMILY_PARTICIPANT.getCode());
        }
    }
    
    /**
     * Update hmily participant status.
     *
     * @param hmilyParticipant the hmily participant
     */
    public static void updateHmilyParticipantStatus(final HmilyParticipant hmilyParticipant) {
        if (Objects.nonNull(hmilyParticipant)) {
            PUBLISHER.publishEvent(hmilyParticipant, EventTypeEnum.UPDATE_HMILY_PARTICIPANT_STATUS.getCode());
        }
    }
    
    /**
     * Remove hmily participant.
     *
     * @param hmilyParticipant the hmily participant
     */
    public static void removeHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        if (Objects.nonNull(hmilyParticipant)) {
            PUBLISHER.publishEvent(hmilyParticipant, EventTypeEnum.REMOVE_HMILY_PARTICIPANT.getCode());
        }
    }
    
    /**
     * Remove hmily participant undo.
     *
     * @param hmilyParticipantUndo the hmily participant undo
     */
    public static void removeHmilyParticipantUndo(final HmilyParticipantUndo hmilyParticipantUndo) {
        PUBLISHER.publishEvent(hmilyParticipantUndo, EventTypeEnum.REMOVE_HMILY_PARTICIPANT_UNDO.getCode());
    }
    
}
