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

import org.dromara.hmily.common.enums.EventTypeEnum;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;

/**
 * The type Hmily repository dispatcher.
 */
public final class HmilyRepositoryDispatcher {
    
    private static final HmilyRepositoryDispatcher INSTANCE = new HmilyRepositoryDispatcher();
    
    private HmilyRepositoryDispatcher() {
    }
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static HmilyRepositoryDispatcher getInstance() {
        return INSTANCE;
    }
    
    /**
     * Do dispatcher.
     *
     * @param event the event
     */
    public void doDispatcher(final HmilyRepositoryEvent event) {
        EventTypeEnum eventTypeEnum = EventTypeEnum.buildByCode(event.getType());
        HmilyTransaction hmilyTransaction = event.getHmilyTransaction();
        HmilyParticipant hmilyParticipant = event.getHmilyParticipant();
        HmilyParticipantUndo hmilyParticipantUndo = event.getHmilyParticipantUndo();
        switch (eventTypeEnum) {
            case CREATE_HMILY_TRANSACTION:
                HmilyRepositoryFacade.getInstance().createHmilyTransaction(event.getHmilyTransaction());
                break;
            case REMOVE_HMILY_TRANSACTION:
                HmilyRepositoryFacade.getInstance().removeHmilyTransaction(hmilyTransaction.getTransId());
                break;
            case UPDATE_HMILY_TRANSACTION_STATUS:
                HmilyRepositoryFacade.getInstance().updateHmilyTransactionStatus(hmilyTransaction.getTransId(), hmilyTransaction.getStatus());
                break;
            case CREATE_HMILY_PARTICIPANT:
                HmilyRepositoryFacade.getInstance().createHmilyParticipant(event.getHmilyParticipant());
                break;
            case UPDATE_HMILY_PARTICIPANT_STATUS:
                HmilyRepositoryFacade.getInstance().updateHmilyParticipantStatus(hmilyParticipant.getParticipantId(), hmilyParticipant.getStatus());
                break;
            case REMOVE_HMILY_PARTICIPANT:
                HmilyRepositoryFacade.getInstance().removeHmilyParticipant(hmilyParticipant.getParticipantId());
                break;
            case REMOVE_HMILY_PARTICIPANT_UNDO:
                HmilyRepositoryFacade.getInstance().removeHmilyParticipantUndo(hmilyParticipantUndo.getUndoId());
                break;
            default:
                break;
        }
    }
}
