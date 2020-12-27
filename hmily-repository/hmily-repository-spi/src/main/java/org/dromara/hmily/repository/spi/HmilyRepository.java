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

package org.dromara.hmily.repository.spi;

import org.dromara.hmily.repository.spi.entity.HmilyLock;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The interface Hmily repository.
 *
 * @author xiaoyu
 */
public interface HmilyRepository {
    
    /**
     * The constant ROWS.
     */
    int ROWS = 1;
    
    /**
     * The constant FAIL_ROWS.
     */
    int FAIL_ROWS = 0;
    
    /**
     * Init.
     *
     * @param appName the app name
     */
    void init(String appName);
    
    /**
     * Sets serializer.
     *
     * @param hmilySerializer the hmily serializer
     */
    void setSerializer(HmilySerializer hmilySerializer);
    
    /**
     * begin global hmilyTransaction.
     *
     * @param hmilyTransaction {@linkplain HmilyTransaction}
     * @return rows 1
     * @throws HmilyRepositoryException the hmily repository exception
     */
    int createHmilyTransaction(HmilyTransaction hmilyTransaction) throws HmilyRepositoryException;
    
    /**
     * Update retry by lock int.
     *
     * @param hmilyTransaction the hmily transaction
     * @return the int
     */
    int updateRetryByLock(HmilyTransaction hmilyTransaction);
    
    /**
     * Find by trans id hmily transaction.
     *
     * @param transId the trans id
     * @return the hmily transaction
     */
    HmilyTransaction findByTransId(Long transId);
    
    /**
     * List limit by delay list.
     *
     * @param date  the date
     * @param limit the limit
     * @return the list
     */
    List<HmilyTransaction> listLimitByDelay(Date date, int limit);
    
    /**
     * Update hmily transaction status int.
     *
     * @param transId the trans id
     * @param status  the status
     * @return the int
     * @throws HmilyRepositoryException the hmily repository exception
     */
    int updateHmilyTransactionStatus(Long transId, Integer status) throws HmilyRepositoryException;
    
    /**
     * Remove hmily transaction int.
     *
     * @param transId the trans id
     * @return the int
     */
    int removeHmilyTransaction(Long transId);
    
    /**
     * Remove hmily transaction by date.
     *
     * @param date the date
     * @return the int
     */
    int removeHmilyTransactionByDate(Date date);
    
    /**
     * Create hmily participant int.
     *
     * @param hmilyParticipant the hmily participant
     * @return the int
     * @throws HmilyRepositoryException the hmily repository exception
     */
    int createHmilyParticipant(HmilyParticipant hmilyParticipant) throws HmilyRepositoryException;
    
    /**
     * Find hmily participant list.
     *
     * @param participantId the participant id
     * @return the list
     */
    List<HmilyParticipant> findHmilyParticipant(Long participantId);
    
    /**
     * List hmily participant list.
     *
     * @param date      the date
     * @param transType the trans type
     * @param limit     the limit
     * @return the list
     */
    List<HmilyParticipant> listHmilyParticipant(Date date, String transType, int limit);
    
    /**
     * List hmily participant by trans id list.
     *
     * @param transId the trans id
     * @return the list
     */
    List<HmilyParticipant> listHmilyParticipantByTransId(Long transId);
    
    /**
     * Exist hmily participant by trans id boolean.
     *
     * @param transId the trans id
     * @return the boolean
     */
    boolean existHmilyParticipantByTransId(Long transId);
    
    /**
     * Update hmily participant status int.
     *
     * @param participantId the participant id
     * @param status        the status
     * @return the int
     * @throws HmilyRepositoryException the hmily repository exception
     */
    int updateHmilyParticipantStatus(Long participantId, Integer status) throws HmilyRepositoryException;
    
    /**
     * Remove hmily participant int.
     *
     * @param participantId the participant id
     * @return the int
     */
    int removeHmilyParticipant(Long participantId);
    
    /**
     * Remove hmily participant by date.
     *
     * @param date the date
     * @return the int
     */
    int removeHmilyParticipantByDate(Date date);
    
    /**
     * Lock hmily participant boolean.
     *
     * @param hmilyParticipant the hmily participant
     * @return the boolean
     */
    boolean lockHmilyParticipant(HmilyParticipant hmilyParticipant);
    
    /**
     * Create hmily participant undo int.
     *
     * @param hmilyParticipantUndo the hmily participant undo
     * @return the int
     */
    int createHmilyParticipantUndo(HmilyParticipantUndo hmilyParticipantUndo);
    
    /**
     * Find hmily participant undo by participant id list.
     *
     * @param participantId the participant id
     * @return the list
     */
    List<HmilyParticipantUndo> findHmilyParticipantUndoByParticipantId(Long participantId);
    
    /**
     * Remove hmily participant undo int.
     *
     * @param undoId the undo id
     * @return the int
     */
    int removeHmilyParticipantUndo(Long undoId);
    
    /**
     * Remove hmily participant undo by date.
     *
     * @param date the date
     * @return the int
     */
    int removeHmilyParticipantUndoByDate(Date date);
    
    /**
     * Update hmily participant undo status int.
     *
     * @param undoId the undo id
     * @param status the status
     * @return the int
     */
    int updateHmilyParticipantUndoStatus(Long undoId, Integer status);
    
    /**
     * Write hmily locks.
     *
     * @param locks locks
     * @return count of writable locks
     */
    int writeHmilyLocks(Collection<HmilyLock> locks);
    
    /**
     * Release hmily locks.
     *
     * @param locks locks
     * @return count of release locks
     */
    int releaseHmilyLocks(Collection<HmilyLock> locks);
    
    /**
     * Find hmily lock by id.
     *
     * @param lockId lock id
     * @return hmily lock
     */
    Optional<HmilyLock> findHmilyLockById(String lockId);
}
