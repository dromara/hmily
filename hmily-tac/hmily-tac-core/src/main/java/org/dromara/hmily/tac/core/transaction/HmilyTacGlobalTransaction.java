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

package org.dromara.hmily.tac.core.transaction;

import java.util.List;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.IdWorkerUtils;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.holder.HmilyTransactionHolder;
import org.dromara.hmily.core.repository.HmilyRepositoryStorage;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;

public class HmilyTacGlobalTransaction {
    
    private static final HmilyTacGlobalTransaction INSTANCE = new HmilyTacGlobalTransaction();
    
    public static HmilyTacGlobalTransaction getInstance() {
        return INSTANCE;
    }
    
    public HmilyTransaction begin() {
        //创建全局的事务，创建一个参与者
        HmilyTransaction globalHmilyTransaction = createHmilyTransaction();
        HmilyRepositoryStorage.createHmilyTransaction(globalHmilyTransaction);
        final HmilyParticipant hmilyParticipant = buildHmilyParticipant(globalHmilyTransaction.getTransId());
        HmilyRepositoryStorage.createHmilyParticipant(hmilyParticipant);
        globalHmilyTransaction.registerParticipant(hmilyParticipant);
        //save tacTransaction in threadLocal
        HmilyTransactionHolder.getInstance().set(globalHmilyTransaction);
        //set TacTransactionContext this context transfer remote
        HmilyTransactionContext context = new HmilyTransactionContext();
        //set action is try
        context.setAction(HmilyActionEnum.TRYING.getCode());
        context.setTransId(globalHmilyTransaction.getTransId());
        context.setRole(HmilyRoleEnum.START.getCode());
        context.setTransType(TransTypeEnum.TCC.name());
        HmilyContextHolder.set(context);
        return globalHmilyTransaction;
    }
    
    public void rollback() {
        HmilyTransaction currentTransaction = HmilyTransactionHolder.getInstance().getCurrentTransaction();
        List<HmilyParticipant> hmilyParticipants = currentTransaction.getHmilyParticipants();
        if(CollectionUtils.isNotEmpty(hmilyParticipants)) {
            for (HmilyParticipant participant : hmilyParticipants) {
                if(participant.getRole() == HmilyRoleEnum.START.getCode()) {
                    //do local
                } else {
                    //do rpc
                }
            }
        }
    }
    
    public void commit() {
    
    }
    
    private HmilyParticipant buildHmilyParticipant(final String transId) {
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setParticipantId(IdWorkerUtils.getInstance().createUUID());
        hmilyParticipant.setTransId(transId);
        hmilyParticipant.setTransType(TransTypeEnum.TAC.name());
        hmilyParticipant.setStatus(HmilyActionEnum.PRE_TRY.getCode());
        hmilyParticipant.setRole(HmilyRoleEnum.START.getCode());
        return hmilyParticipant;
    }
    
    private HmilyTransaction createHmilyTransaction() {
        HmilyTransaction hmilyTransaction = new HmilyTransaction();
        hmilyTransaction.setTransId(IdWorkerUtils.getInstance().createUUID());
        hmilyTransaction.setStatus(HmilyActionEnum.PRE_TRY.getCode());
        hmilyTransaction.setTransType(TransTypeEnum.TAC.name());
        return hmilyTransaction;
    }
}
