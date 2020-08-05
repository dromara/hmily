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

import java.lang.reflect.Method;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.common.enums.ExecutorTypeEnum;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.IdWorkerUtils;
import org.dromara.hmily.core.cache.HmilyParticipantCacheManager;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.hook.UndoHook;
import org.dromara.hmily.core.reflect.HmilyReflector;
import org.dromara.hmily.core.repository.HmilyRepositoryStorage;
import org.dromara.hmily.repository.spi.entity.HmilyInvocation;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.tac.core.cache.HmilyParticipantUndoCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Hmily tac participant transaction.
 *
 * @author xiaoyu
 */
public class HmilyTacParticipantTransaction {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyTacParticipantTransaction.class);
    
    private static final HmilyTacParticipantTransaction INSTANCE = new HmilyTacParticipantTransaction();
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static HmilyTacParticipantTransaction getInstance() {
        return INSTANCE;
    }
    
    /**
     * Begin hmily transaction.
     *
     * @param context the context
     * @param point   the point
     * @return the hmily transaction
     */
    public HmilyParticipant beginParticipant(final HmilyTransactionContext context, final ProceedingJoinPoint point) {
        //创建全局的事务，创建一个参与者
        final HmilyParticipant hmilyParticipant = buildHmilyParticipant(point, context.getParticipantId(), context.getParticipantRefId(), context.getTransId());
        HmilyParticipantCacheManager.getInstance().cacheHmilyParticipant(hmilyParticipant);
        HmilyRepositoryStorage.createHmilyParticipant(hmilyParticipant);
        context.setRole(HmilyRoleEnum.PARTICIPANT.getCode());
        HmilyContextHolder.set(context);
        return hmilyParticipant;
        
    }
    
    /**
     * Rollback participant.
     *
     * @param hmilyParticipantList the hmily participant list
     * @param selfParticipantId    the self participant id
     */
    public void rollbackParticipant(final List<HmilyParticipant> hmilyParticipantList, final Long selfParticipantId) {
        if (CollectionUtils.isEmpty(hmilyParticipantList)) {
            return;
        }
        for (HmilyParticipant participant : hmilyParticipantList) {
            try {
                if (participant.getParticipantId().equals(selfParticipantId)) {
                    //do local
                    List<HmilyParticipantUndo> undoList = HmilyParticipantUndoCacheManager.getInstance().get(participant.getParticipantId());
                    for (HmilyParticipantUndo undo : undoList) {
                        boolean success = UndoHook.INSTANCE.run(undo);
                        if (success) {
                            HmilyRepositoryStorage.removeHmilyParticipantUndo(undo);
                        }
                    }
                    cleanHmilyParticipant(participant);
                } else {
                    HmilyReflector.executor(HmilyActionEnum.CANCELING, ExecutorTypeEnum.RPC, participant);
                }
            } catch (Throwable e) {
                LOGGER.error("HmilyParticipant rollback exception :{} ", participant.toString());
                throw new HmilyRuntimeException(" hmilyParticipant execute rollback exception:" + participant.toString());
            } finally {
                HmilyContextHolder.remove();
            }
        }
    }
    
    
    /**
     * Commit participant.
     *
     * @param hmilyParticipantList the hmily participant list
     * @param selfParticipantId    the self participant id
     */
    public void commitParticipant(final List<HmilyParticipant> hmilyParticipantList, final Long selfParticipantId) {
        if (CollectionUtils.isEmpty(hmilyParticipantList)) {
            return;
        }
        for (HmilyParticipant hmilyParticipant : hmilyParticipantList) {
            try {
                if (hmilyParticipant.getParticipantId().equals(selfParticipantId)) {
                    //do local
                    List<HmilyParticipantUndo> undoList = HmilyParticipantUndoCacheManager.getInstance().get(hmilyParticipant.getParticipantId());
                    for (HmilyParticipantUndo undo : undoList) {
                        //clean undo
                        HmilyRepositoryStorage.removeHmilyParticipantUndo(undo);
                    }
                    cleanHmilyParticipant(hmilyParticipant);
                } else {
                    HmilyReflector.executor(HmilyActionEnum.CONFIRMING, ExecutorTypeEnum.RPC, hmilyParticipant);
                }
            } catch (Throwable throwable) {
                throw new HmilyRuntimeException(" hmilyParticipant execute confirm exception:" + hmilyParticipant.toString());
            } finally {
                HmilyContextHolder.remove();
            }
        }
    }
    
    private void cleanHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        HmilyParticipantCacheManager.getInstance().removeByKey(hmilyParticipant.getParticipantId());
        HmilyRepositoryStorage.removeHmilyParticipant(hmilyParticipant);
    }
    
    private HmilyParticipant buildHmilyParticipant(final ProceedingJoinPoint point, final Long participantId, final Long participantRefId, final Long transId) {
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        if (null == participantId) {
            hmilyParticipant.setParticipantId(IdWorkerUtils.getInstance().createUUID());
        } else {
            hmilyParticipant.setParticipantId(participantId);
        }
        if (null != participantRefId) {
            hmilyParticipant.setParticipantRefId(participantRefId);
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Class<?> clazz = point.getTarget().getClass();
            Object[] args = point.getArgs();
            HmilyInvocation hmilyInvocation = new HmilyInvocation(clazz.getInterfaces()[0], method.getName(), method.getParameterTypes(), args);
            hmilyParticipant.setConfirmHmilyInvocation(hmilyInvocation);
        }
        hmilyParticipant.setTransId(transId);
        hmilyParticipant.setTransType(TransTypeEnum.TAC.name());
        hmilyParticipant.setStatus(HmilyActionEnum.PRE_TRY.getCode());
        hmilyParticipant.setRole(HmilyRoleEnum.PARTICIPANT.getCode());
        return hmilyParticipant;
    }
}
