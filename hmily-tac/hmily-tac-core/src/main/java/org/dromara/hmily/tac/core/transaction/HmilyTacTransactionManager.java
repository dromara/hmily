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

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.common.enums.ExecutorTypeEnum;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.IdWorkerUtils;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.holder.HmilyTransactionHolder;
import org.dromara.hmily.core.reflect.HmilyReflector;
import org.dromara.hmily.core.repository.HmilyRepositoryStorage;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;

import java.util.List;
import java.util.Objects;

/**
 * Hmily Tac transaction manager.
 *
 * @author xiaoyu
 */
@Slf4j
public class HmilyTacTransactionManager {
    
    private static final HmilyTacTransactionManager INSTANCE = new HmilyTacTransactionManager();
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static HmilyTacTransactionManager getInstance() {
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
        context.setParticipantId(hmilyParticipant.getParticipantId());
        HmilyContextHolder.set(context);
        log.debug("TAC-tm-begin ::: {}", globalHmilyTransaction);
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
        log.debug("TAC-tm-rollback ::: {}", currentTransaction);
        List<HmilyParticipant> hmilyParticipants = currentTransaction.getHmilyParticipants();
        if (CollectionUtils.isEmpty(hmilyParticipants)) {
            return;
        }
        List<Boolean> successList = Lists.newArrayList();
        for (HmilyParticipant participant : hmilyParticipants) {
            try {
                if (participant.getRole() == HmilyRoleEnum.START.getCode()) {
                    HmilyTacLocalParticipantExecutor.cancel(participant);
                } else {
                    HmilyReflector.executor(HmilyActionEnum.CANCELING, ExecutorTypeEnum.RPC, participant);
                }
                successList.add(true);
            } catch (Throwable e) {
                successList.add(false);
                log.error("HmilyParticipant rollback exception :{} ", participant.toString());
            } finally {
                HmilyContextHolder.remove();
            }
        }
        if (successList.stream().allMatch(e -> e)) {
            // remove global
            HmilyRepositoryStorage.removeHmilyTransaction(currentTransaction);
        }
    }
    
    /**
     * Commit.
     *
     * @param currentTransaction the current transaction
     */
    public void commit(final HmilyTransaction currentTransaction) {
        log.debug("TAC-tm-commit ::: {}", currentTransaction);
        if (Objects.isNull(currentTransaction)) {
            return;
        }
        List<HmilyParticipant> hmilyParticipants = currentTransaction.getHmilyParticipants();
        if (CollectionUtils.isEmpty(hmilyParticipants)) {
            return;
        }
        List<Boolean> successList = Lists.newArrayList();
        for (HmilyParticipant participant : hmilyParticipants) {
            try {
                if (participant.getRole() == HmilyRoleEnum.START.getCode()) {
                    HmilyTacLocalParticipantExecutor.confirm(participant);
                } else {
                    HmilyReflector.executor(HmilyActionEnum.CONFIRMING, ExecutorTypeEnum.RPC, participant);
                }
                successList.add(true);
            } catch (Throwable e) {
                successList.add(false);
                log.error("HmilyParticipant rollback exception :{} ", participant.toString());
            } finally {
                HmilyContextHolder.remove();
            }
        }
        if (successList.stream().allMatch(e -> e)) {
            // remove global
            HmilyRepositoryStorage.removeHmilyTransaction(currentTransaction);
        }
    }
    
    /**
     * Remove.
     */
    public void remove() {
        HmilyTransactionHolder.getInstance().remove();
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
