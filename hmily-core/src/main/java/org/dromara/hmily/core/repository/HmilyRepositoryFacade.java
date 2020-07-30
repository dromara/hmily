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
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;

/**
 * The type Hmily coordinator facade.
 *
 * @author xiaoyu
 */
public class HmilyRepositoryFacade {
    
    private static final HmilyRepositoryFacade INSTANCE = new HmilyRepositoryFacade();
    
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
    
    public String createHmilyTransaction(final HmilyTransaction hmilyTransaction) {
        final int rows = hmilyRepository.createHmilyTransaction(hmilyTransaction);
        if (rows > 0) {
            return hmilyTransaction.getTransId();
        }
        throw new HmilyRepositoryException();
    }
    
    public int updateHmilyTransactionStatus(final String transId, final Integer status) {
        return hmilyRepository.updateHmilyTransactionStatus(transId, status);
    }
    
    public HmilyTransaction findHmilyTransactionByTransId(final String transId) {
        return null;
    }
    
    public boolean removeHmilyTransaction(final String transId) {
        return hmilyRepository.removeHmilyTransaction(transId) > 0;
    }
    
    public void updateHmilyTransaction(final HmilyTransaction hmilyTransaction) {
    
    }
    
    public boolean createHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        //return coordinatorRepository.updateParticipant(hmilyTransaction);
        return hmilyRepository.createHmilyParticipant(hmilyParticipant) > 0;
    }
    
    public int updateHmilyParticipantStatus(final String transId, final Integer status) {
        return hmilyRepository.updateHmilyParticipantStatus(transId, status);
    }
    
    public boolean removeHmilyParticipant(final String participantId) {
        return hmilyRepository.removeHmilyParticipant(participantId) > 0;
    }
    
    public List<HmilyParticipant> findHmilyParticipant(String participantId) {
        return hmilyRepository.findHmilyParticipant(participantId);
    }
    
   
}
