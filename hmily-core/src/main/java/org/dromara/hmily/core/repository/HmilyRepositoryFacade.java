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
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;

/**
 * The type Hmily coordinator facade.
 *
 * @author xiaoyu
 */
public final class HmilyRepositoryFacade {
    
    private static final HmilyRepositoryFacade INSTANCE = new HmilyRepositoryFacade();
    
    private final HmilyConfig hmilyConfig = ConfigEnv.getInstance().getConfig(HmilyConfig.class);
    
    @Setter
    private HmilyRepository hmilyRepository;
    
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
     */
    public void createHmilyTransaction(final HmilyTransaction hmilyTransaction) {
        checkRows(hmilyRepository.createHmilyTransaction(hmilyTransaction));
    }
    
    /**
     * Update hmily transaction status int.
     *
     * @param transId the trans id
     * @param status  the status
     */
    public void updateHmilyTransactionStatus(final Long transId, final Integer status) {
        checkRows(hmilyRepository.updateHmilyTransactionStatus(transId, status));
    }
    
    /**
     * Remove hmily transaction.
     *
     * @param transId the trans id
     */
    public void removeHmilyTransaction(final Long transId) {
        if (hmilyConfig.isPhyDeleted()) {
            checkRows(hmilyRepository.removeHmilyTransaction(transId));
        } else {
            updateHmilyTransactionStatus(transId, HmilyActionEnum.DELETE.getCode());
        }
    }
    
    /**
     * Create hmily participant.
     *
     * @param hmilyParticipant the hmily participant
     */
    public void createHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        checkRows(hmilyRepository.createHmilyParticipant(hmilyParticipant));
    }
    
    /**
     * Update hmily participant status.
     *
     * @param transId the trans id
     * @param status  the status
     */
    public void updateHmilyParticipantStatus(final Long transId, final Integer status) {
        checkRows(hmilyRepository.updateHmilyParticipantStatus(transId, status));
    }
    
    /**
     * Remove hmily participant.
     *
     * @param participantId the participant id
     */
    public void removeHmilyParticipant(final Long participantId) {
        if (hmilyConfig.isPhyDeleted()) {
            checkRows(hmilyRepository.removeHmilyParticipant(participantId));
        } else {
            updateHmilyParticipantStatus(participantId, HmilyActionEnum.DELETE.getCode());
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
     * Create hmily participant undo.
     *
     * @param undo the undo
     */
    public void createHmilyParticipantUndo(final HmilyParticipantUndo undo) {
        checkRows(hmilyRepository.createHmilyParticipantUndo(undo));
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
     * Remove hmily participant undo.
     *
     * @param undoId the undo id
     */
    public void removeHmilyParticipantUndo(final Long undoId) {
        if (hmilyConfig.isPhyDeleted()) {
            checkRows(hmilyRepository.removeHmilyParticipantUndo(undoId));
        } else {
            updateHmilyParticipantUndoStatus(undoId, HmilyActionEnum.DELETE.getCode());
        }
    }
    
    /**
     * Update hmily participant undo status.
     *
     * @param undoId the undo id
     * @param status the status
     */
    public void updateHmilyParticipantUndoStatus(final Long undoId, final Integer status) {
        checkRows(hmilyRepository.updateHmilyParticipantUndoStatus(undoId, status));
    }
    
    private void checkRows(final int rows) {
        if (rows != 1) {
            throw new HmilyRepositoryException("hmily repository have exception");
        }
    }
}
