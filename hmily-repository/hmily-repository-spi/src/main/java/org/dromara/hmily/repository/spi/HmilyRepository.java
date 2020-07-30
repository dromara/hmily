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

import java.util.Date;
import java.util.List;
import org.dromara.hmily.config.HmilyConfig;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;

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
     * @param hmilyConfig the hmily config
     */
    void init(HmilyConfig hmilyConfig);
    
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
    HmilyTransaction findByTransId(String transId);
    
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
    int updateHmilyTransactionStatus(String transId, Integer status) throws HmilyRepositoryException;
    
    /**
     * Remove hmily transaction int.
     *
     * @param transId the trans id
     * @return the int
     */
    int removeHmilyTransaction(String transId);
    
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
    List<HmilyParticipant> findHmilyParticipant(String participantId);
    
    /**
     * List hmily participant list.
     *
     * @param date  the date
     * @param limit the limit
     * @return the list
     */
    List<HmilyParticipant> listHmilyParticipant(Date date, int limit);
    
    /**
     * Update hmily participant status int.
     *
     * @param participantId the participant id
     * @param status        the status
     * @return the int
     * @throws HmilyRepositoryException the hmily repository exception
     */
    int updateHmilyParticipantStatus(String participantId, Integer status) throws HmilyRepositoryException;
    
    /**
     * Remove hmily participant int.
     *
     * @param participantId the participant id
     * @return the int
     */
    int removeHmilyParticipant(String participantId);
    
    /**
     * Lock hmily participant boolean.
     *
     * @param hmilyParticipant the hmily participant
     * @return the boolean
     */
    boolean lockHmilyParticipant(HmilyParticipant hmilyParticipant);
    
}
