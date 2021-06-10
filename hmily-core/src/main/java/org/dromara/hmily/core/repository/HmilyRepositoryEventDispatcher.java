/*
 * Copyright 2017-2021 Dromara.org

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * The type Hmily repository event dispatcher.
 */
public final class HmilyRepositoryEventDispatcher {
    
    private static final HmilyRepositoryEventDispatcher INSTANCE = new HmilyRepositoryEventDispatcher();
    
    private HmilyRepositoryEventDispatcher() {
    }
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static HmilyRepositoryEventDispatcher getInstance() {
        return INSTANCE;
    }
    
    /**
     * Do event dispatch.
     *
     * @param event the event
     */
    public void doDispatch(final HmilyRepositoryEvent event) {
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
            case CREATE_HMILY_PARTICIPANT_UNDO:
                HmilyRepositoryFacade.getInstance().createHmilyParticipantUndo(hmilyParticipantUndo);
                break;
            case REMOVE_HMILY_PARTICIPANT_UNDO:
                HmilyRepositoryFacade.getInstance().removeHmilyParticipantUndo(hmilyParticipantUndo.getUndoId());
                break;
            case WRITE_HMILY_LOCKS:
                HmilyRepositoryFacade.getInstance().writeHmilyLocks(event.getHmilyLocks());
                break;
            case RELEASE_HMILY_LOCKS:
                HmilyRepositoryFacade.getInstance().releaseHmilyLocks(event.getHmilyLocks());
                break;
            default:
                break;
        }
    }
}
