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
import java.util.Objects;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.common.enums.ExecutorTypeEnum;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.IdWorkerUtils;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.holder.HmilyTransactionHolder;
import org.dromara.hmily.core.hook.UndoHook;
import org.dromara.hmily.core.reflect.HmilyReflector;
import org.dromara.hmily.core.repository.HmilyRepositoryStorage;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.tac.core.cache.HmilyParticipantUndoCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Hmily tac global transaction.
 *
 * @author xiaoyu
 */
public class HmilyTacStarterTransaction {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyTacStarterTransaction.class);
    
    private static final HmilyTacStarterTransaction INSTANCE = new HmilyTacStarterTransaction();
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static HmilyTacStarterTransaction getInstance() {
        return INSTANCE;
    }
    
    /**
     * Begin hmily transaction.
     *
     * @return the hmily transaction
     */
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
        context.setTransType(TransTypeEnum.TAC.name());
        HmilyContextHolder.set(context);
        return globalHmilyTransaction;
    }
    
    /**
     * Rollback.
     */
    public void rollback() {
        rollback(getHmilyTransaction());
    }
    
    /**
     * Rollback.
     *
     * @param currentTransaction the current transaction
     */
    public void rollback(final HmilyTransaction currentTransaction) {
        if (Objects.isNull(currentTransaction)) {
            return;
        }
        List<HmilyParticipant> hmilyParticipants = currentTransaction.getHmilyParticipants();
        if (CollectionUtils.isEmpty(hmilyParticipants)) {
            return;
        }
        for (HmilyParticipant participant : hmilyParticipants) {
            try {
                if (participant.getRole() == HmilyRoleEnum.START.getCode()) {
                    //do local
                    List<HmilyParticipantUndo> undoList = HmilyParticipantUndoCacheManager.getInstance().get(participant.getParticipantId());
                    for (HmilyParticipantUndo undo : undoList) {
                        boolean success = UndoHook.INSTANCE.run(undo);
                        if (success) {
                            cleanUndo(undo);
                        }
                    }
                } else {
                    HmilyReflector.executor(HmilyActionEnum.CANCELING, ExecutorTypeEnum.RPC, participant);
                }
            } catch (Throwable e) {
                LOGGER.error("HmilyParticipant rollback exception :{} ", participant.toString());
            } finally {
                HmilyContextHolder.remove();
            }
        }
        // maybe remove participant
    }
    
    /**
     * Commit.
     */
    public void commit() {
        commit(getHmilyTransaction());
    }
    
    /**
     * Commit.
     *
     * @param currentTransaction the current transaction
     */
    public void commit(final HmilyTransaction currentTransaction) {
        if (Objects.isNull(currentTransaction)) {
            return;
        }
        List<HmilyParticipant> hmilyParticipants = currentTransaction.getHmilyParticipants();
        if (CollectionUtils.isEmpty(hmilyParticipants)) {
            return;
        }
        for (HmilyParticipant participant : hmilyParticipants) {
            try {
                if (participant.getRole() == HmilyRoleEnum.START.getCode()) {
                    //do local
                    List<HmilyParticipantUndo> undoList = HmilyParticipantUndoCacheManager.getInstance().get(participant.getParticipantId());
                    for (HmilyParticipantUndo undo : undoList) {
                        //clean undo
                        cleanUndo(undo);
                    }
                } else {
                    HmilyReflector.executor(HmilyActionEnum.CONFIRMING, ExecutorTypeEnum.RPC, participant);
                }
            } catch (Throwable e) {
                LOGGER.error("HmilyParticipant rollback exception :{} ", participant.toString());
            } finally {
                HmilyContextHolder.remove();
            }
        }
    }
    
    /**
     * Remove.
     */
    public void remove() {
        HmilyTransactionHolder.getInstance().remove();
    }
    
    private void cleanUndo(final HmilyParticipantUndo hmilyParticipantUndo) {
        //clean undo
        HmilyRepositoryStorage.removeHmilyParticipantUndo(hmilyParticipantUndo);
        HmilyParticipantUndoCacheManager.getInstance().removeByKey(hmilyParticipantUndo.getParticipantId());
    }
    
    /**
     * Gets hmily transaction.
     *
     * @return the hmily transaction
     */
    public HmilyTransaction getHmilyTransaction() {
        return HmilyTransactionHolder.getInstance().getCurrentTransaction();
    }
    
    private HmilyParticipant buildHmilyParticipant(final Long transId) {
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setParticipantId(IdWorkerUtils.getInstance().createUUID());
        hmilyParticipant.setTransId(transId);
        hmilyParticipant.setTransType(TransTypeEnum.TAC.name());
        hmilyParticipant.setStatus(HmilyActionEnum.TRYING.getCode());
        hmilyParticipant.setRole(HmilyRoleEnum.START.getCode());
        return hmilyParticipant;
    }
    
    private HmilyTransaction createHmilyTransaction() {
        HmilyTransaction hmilyTransaction = new HmilyTransaction();
        hmilyTransaction.setTransId(IdWorkerUtils.getInstance().createUUID());
        hmilyTransaction.setStatus(HmilyActionEnum.TRYING.getCode());
        hmilyTransaction.setTransType(TransTypeEnum.TAC.name());
        return hmilyTransaction;
    }
}
