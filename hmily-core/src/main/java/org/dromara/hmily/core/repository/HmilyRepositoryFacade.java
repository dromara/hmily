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

import java.util.List;
import lombok.Setter;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;

/**
 * The type Hmily coordinator facade.
 *
 * @author xiaoyu
 */
public final class HmilyRepositoryFacade {
    
    private static final HmilyRepositoryFacade INSTANCE = new HmilyRepositoryFacade();
    
    @Setter
    private HmilyRepository hmilyRepository;
    
    @Setter
    private boolean phyDeleted;
    
    private HmilyRepositoryFacade() {
    }
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static HmilyRepositoryFacade getInstance() {
        return INSTANCE;
    }
    
    /**
     * Create hmily transaction string.
     *
     * @param hmilyTransaction the hmily transaction
     * @return the string
     */
    public boolean createHmilyTransaction(final HmilyTransaction hmilyTransaction) {
        return hmilyRepository.createHmilyTransaction(hmilyTransaction) > 0;
    }
    
    /**
     * Update hmily transaction status int.
     *
     * @param transId the trans id
     * @param status  the status
     * @return the int
     */
    public boolean updateHmilyTransactionStatus(final Long transId, final Integer status) {
        return hmilyRepository.updateHmilyTransactionStatus(transId, status) > 0;
    }
    
    /**
     * Remove hmily transaction boolean.
     *
     * @param transId the trans id
     * @return the boolean
     */
    public boolean removeHmilyTransaction(final Long transId) {
        if (phyDeleted) {
            return hmilyRepository.removeHmilyTransaction(transId) > 0;
        } else {
            return updateHmilyTransactionStatus(transId, HmilyActionEnum.DELETE.getCode());
        }
    }
    
    /**
     * Create hmily participant boolean.
     *
     * @param hmilyParticipant the hmily participant
     * @return the boolean
     */
    public boolean createHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        return hmilyRepository.createHmilyParticipant(hmilyParticipant) > 0;
    }
    
    /**
     * Update hmily participant status int.
     *
     * @param transId the trans id
     * @param status  the status
     * @return the int
     */
    public boolean updateHmilyParticipantStatus(final Long transId, final Integer status) {
        return hmilyRepository.updateHmilyParticipantStatus(transId, status) > 0;
    }
    
    /**
     * Remove hmily participant boolean.
     *
     * @param participantId the participant id
     * @return the boolean
     */
    public boolean removeHmilyParticipant(final Long participantId) {
        if (phyDeleted) {
            return hmilyRepository.removeHmilyParticipant(participantId) > 0;
        } else {
            return updateHmilyParticipantStatus(participantId, HmilyActionEnum.DELETE.getCode());
        }
    }
    
    /**
     * Find hmily participant list.
     *
     * @param participantId the participant id
     * @return the list
     */
    public List<HmilyParticipant> findHmilyParticipant(final Long participantId) {
        return hmilyRepository.findHmilyParticipant(participantId);
    }
    
    /**
     * Create hmily participant undo boolean.
     *
     * @param undo the undo
     * @return the boolean
     */
    public boolean createHmilyParticipantUndo(final HmilyParticipantUndo undo) {
        return hmilyRepository.createHmilyParticipantUndo(undo) > 0;
    }
    
    /**
     * Find undo by participant id list.
     *
     * @param participantId the participant id
     * @return the list
     */
    public List<HmilyParticipantUndo> findUndoByParticipantId(final Long participantId) {
        return hmilyRepository.findHmilyParticipantUndoByParticipantId(participantId);
    }
    
    /**
     * Remove hmily participant undo boolean.
     *
     * @param undoId the undo id
     * @return the boolean
     */
    public boolean removeHmilyParticipantUndo(final Long undoId) {
        if (phyDeleted) {
            return hmilyRepository.removeHmilyParticipantUndo(undoId) > 0;
        } else {
            return updateHmilyParticipantUndoStatus(undoId, HmilyActionEnum.DELETE.getCode());
        }
    }
    
    /**
     * Update hmily participant undo status boolean.
     *
     * @param undoId the undo id
     * @param status the status
     * @return the boolean
     */
    public boolean updateHmilyParticipantUndoStatus(final Long undoId, final Integer status) {
        return hmilyRepository.updateHmilyParticipantUndoStatus(undoId, status) > 0;
    }
}
