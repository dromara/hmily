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

package org.dromara.hmily.tcc.executor;

import com.google.common.collect.Lists;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.common.enums.EventTypeEnum;
import org.dromara.hmily.common.enums.ExecutorTypeEnum;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.IdWorkerUtils;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.core.cache.HmilyParticipantCacheManager;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.disruptor.publisher.HmilyRepositoryEventPublisher;
import org.dromara.hmily.core.holder.HmilyTransactionHolder;
import org.dromara.hmily.core.reflect.HmilyReflector;
import org.dromara.hmily.repository.spi.entity.HmilyInvocation;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * this is hmily transaction manager.
 *
 * @author xiaoyu
 */
public final class HmilyTccTransactionExecutor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyTccTransactionExecutor.class);
    
    private static final HmilyTccTransactionExecutor INSTANCE = new HmilyTccTransactionExecutor();
    
    private static final HmilyRepositoryEventPublisher PUBLISHER = HmilyRepositoryEventPublisher.getInstance();
    
    private HmilyTccTransactionExecutor() {
    }
    
    public static HmilyTccTransactionExecutor getInstance() {
        return INSTANCE;
    }
    
    /**
     * transaction preTry.
     *
     * @param point cut point.
     * @return TccTransaction hmily transaction
     */
    public HmilyTransaction preTry(final ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, () -> "......hmily tcc transaction starter....");
        //build tccTransaction
        HmilyTransaction hmilyTransaction = createHmilyTransaction();
        PUBLISHER.publishEvent(hmilyTransaction, EventTypeEnum.CREATE_HMILY_TRANSACTION.getCode());
        HmilyParticipant hmilyParticipant = buildHmilyParticipant(point, null, null, HmilyRoleEnum.START.getCode(), hmilyTransaction.getTransId());
        Optional.ofNullable(hmilyParticipant).ifPresent(h -> PUBLISHER.publishEvent(h, EventTypeEnum.CREATE_HMILY_PARTICIPANT.getCode()));
        hmilyTransaction.registerParticipant(hmilyParticipant);
        //save tccTransaction in threadLocal
        HmilyTransactionHolder.getInstance().set(hmilyTransaction);
        //set TccTransactionContext this context transfer remote
        HmilyTransactionContext context = new HmilyTransactionContext();
        //set action is try
        context.setAction(HmilyActionEnum.TRYING.getCode());
        context.setTransId(hmilyTransaction.getTransId());
        context.setRole(HmilyRoleEnum.START.getCode());
        context.setTransType(TransTypeEnum.TCC.name());
        HmilyContextHolder.set(context);
        return hmilyTransaction;
    }
    
    /**
     * this is Participant transaction preTry.
     *
     * @param context transaction context.
     * @param point   cut point
     * @return TccTransaction hmily transaction
     */
    public HmilyParticipant preTryParticipant(final HmilyTransactionContext context, final ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, "participant hmily tcc transaction start..：{}", context::toString);
        final HmilyParticipant hmilyParticipant = buildHmilyParticipant(point, context.getParticipantId(), context.getParticipantRefId(), HmilyRoleEnum.PARTICIPANT.getCode(), context.getTransId());
        //cache by guava
        if (Objects.nonNull(hmilyParticipant)) {
            HmilyParticipantCacheManager.getInstance().cacheHmilyParticipant(hmilyParticipant);
            PUBLISHER.publishEvent(hmilyParticipant, EventTypeEnum.CREATE_HMILY_PARTICIPANT.getCode());
        }
        //publishEvent
        //Nested transaction support
        context.setRole(HmilyRoleEnum.PARTICIPANT.getCode());
        HmilyContextHolder.set(context);
        return hmilyParticipant;
    }
    
    /**
     * Call the confirm method and basically if the initiator calls here call the remote or the original method
     * However, the context sets the call confirm
     * The remote service calls the confirm method.
     *
     * @param currentTransaction {@linkplain HmilyTransaction}
     * @throws HmilyRuntimeException ex
     */
    public void globalConfirm(final HmilyTransaction currentTransaction) throws HmilyRuntimeException {
        LogUtil.debug(LOGGER, () -> "hmily transaction confirm .......！start");
        if (Objects.isNull(currentTransaction) || CollectionUtils.isEmpty(currentTransaction.getHmilyParticipants())) {
            return;
        }
        currentTransaction.setStatus(HmilyActionEnum.CONFIRMING.getCode());
        updateHmilyTransactionStatus(currentTransaction);
        final List<HmilyParticipant> hmilyParticipants = currentTransaction.getHmilyParticipants();
        for (HmilyParticipant hmilyParticipant : hmilyParticipants) {
            try {
                if (hmilyParticipant.getRole() == HmilyRoleEnum.START.getCode()) {
                    HmilyReflector.executor(HmilyActionEnum.CONFIRMING, ExecutorTypeEnum.LOCAL, hmilyParticipant);
                    removeHmilyParticipant(hmilyParticipant);
                } else {
                    HmilyReflector.executor(HmilyActionEnum.CONFIRMING, ExecutorTypeEnum.RPC, hmilyParticipant);
                }
            } catch (Throwable e) {
                LOGGER.error("HmilyParticipant confirm exception :{} ", hmilyParticipant.toString());
            } finally {
                HmilyContextHolder.remove();
            }
        }
    }
    
    public Object participantConfirm(final List<HmilyParticipant> hmilyParticipantList, final Long selfParticipantId) {
        if (CollectionUtils.isEmpty(hmilyParticipantList)) {
            return null;
        }
        List<Object> results = Lists.newArrayListWithCapacity(hmilyParticipantList.size());
        for (HmilyParticipant hmilyParticipant : hmilyParticipantList) {
            try {
                if (hmilyParticipant.getParticipantId().equals(selfParticipantId)) {
                    final Object result = HmilyReflector.executor(HmilyActionEnum.CONFIRMING, ExecutorTypeEnum.LOCAL, hmilyParticipant);
                    results.add(result);
                    removeHmilyParticipant(hmilyParticipant);
                } else {
                    final Object result = HmilyReflector.executor(HmilyActionEnum.CONFIRMING, ExecutorTypeEnum.RPC, hmilyParticipant);
                    results.add(result);
                }
            } catch (Throwable throwable) {
                throw new HmilyRuntimeException(" hmilyParticipant execute confirm exception:" + hmilyParticipant.toString());
            } finally {
                HmilyContextHolder.remove();
            }
        }
        HmilyParticipantCacheManager.getInstance().removeByKey(selfParticipantId);
        return results.get(0);
    }
    
    /**
     * cancel transaction.
     *
     * @param currentTransaction {@linkplain HmilyTransaction}
     */
    public void globalCancel(final HmilyTransaction currentTransaction) {
        LogUtil.debug(LOGGER, () -> "tcc cancel ...........start!");
        if (Objects.isNull(currentTransaction) || CollectionUtils.isEmpty(currentTransaction.getHmilyParticipants())) {
            return;
        }
        currentTransaction.setStatus(HmilyActionEnum.CANCELING.getCode());
        //update cancel
        updateHmilyTransactionStatus(currentTransaction);
        final List<HmilyParticipant> hmilyParticipants = currentTransaction.getHmilyParticipants();
        for (HmilyParticipant hmilyParticipant : hmilyParticipants) {
            try {
                if (hmilyParticipant.getRole() == HmilyRoleEnum.START.getCode()) {
                    HmilyReflector.executor(HmilyActionEnum.CANCELING, ExecutorTypeEnum.LOCAL, hmilyParticipant);
                    removeHmilyParticipant(hmilyParticipant);
                } else {
                    HmilyReflector.executor(HmilyActionEnum.CANCELING, ExecutorTypeEnum.RPC, hmilyParticipant);
                }
            } catch (Throwable e) {
                LOGGER.error("HmilyParticipant cancel exception :{}", hmilyParticipant.toString(), e);
            } finally {
                HmilyContextHolder.remove();
            }
        }
    }
    
    public Object participantCancel(final List<HmilyParticipant> hmilyParticipants, final Long selfParticipantId) {
        LogUtil.debug(LOGGER, () -> "tcc cancel ...........start!");
        if (CollectionUtils.isEmpty(hmilyParticipants)) {
            return null;
        }
        //if cc pattern，can not execute cancel
        //update cancel
        HmilyParticipant selfHmilyParticipant = filterSelfHmilyParticipant(hmilyParticipants);
        if (Objects.nonNull(selfHmilyParticipant)) {
            selfHmilyParticipant.setStatus(HmilyActionEnum.CANCELING.getCode());
            updateHmilyParticipantStatus(selfHmilyParticipant);
        }
        List<Object> results = Lists.newArrayListWithCapacity(hmilyParticipants.size());
        for (HmilyParticipant hmilyParticipant : hmilyParticipants) {
            try {
                if (hmilyParticipant.getParticipantId().equals(selfParticipantId)) {
                    final Object result = HmilyReflector.executor(HmilyActionEnum.CANCELING, ExecutorTypeEnum.LOCAL, hmilyParticipant);
                    results.add(result);
                    removeHmilyParticipant(hmilyParticipant);
                } else {
                    final Object result = HmilyReflector.executor(HmilyActionEnum.CANCELING, ExecutorTypeEnum.RPC, hmilyParticipant);
                    results.add(result);
                }
            } catch (Throwable throwable) {
                throw new HmilyRuntimeException(" hmilyParticipant execute cancel exception:" + hmilyParticipant.toString());
            } finally {
                HmilyContextHolder.remove();
            }
        }
        HmilyParticipantCacheManager.getInstance().removeByKey(selfParticipantId);
        return results.get(0);
    }
    
    /**
     * update transaction status by disruptor.
     *
     * @param hmilyTransaction {@linkplain HmilyTransaction}
     */
    public void updateStartStatus(final HmilyTransaction hmilyTransaction) {
        updateHmilyTransactionStatus(hmilyTransaction);
        HmilyParticipant hmilyParticipant = filterStartHmilyParticipant(hmilyTransaction);
        if (Objects.nonNull(hmilyParticipant)) {
            hmilyParticipant.setStatus(hmilyTransaction.getStatus());
            updateHmilyParticipantStatus(hmilyParticipant);
        }
    }
    
    public void updateHmilyTransactionStatus(final HmilyTransaction hmilyTransaction) {
        if (Objects.nonNull(hmilyTransaction)) {
            PUBLISHER.publishEvent(hmilyTransaction, EventTypeEnum.UPDATE_HMILY_TRANSACTION_STATUS.getCode());
        }
    }
    
    public void updateHmilyParticipantStatus(final HmilyParticipant hmilyParticipant) {
        if (Objects.nonNull(hmilyParticipant)) {
            PUBLISHER.publishEvent(hmilyParticipant, EventTypeEnum.UPDATE_HMILY_PARTICIPANT_STATUS.getCode());
        }
    }
    
    public void removeHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        if (null != hmilyParticipant) {
            PUBLISHER.publishEvent(hmilyParticipant, EventTypeEnum.REMOVE_HMILY_PARTICIPANT.getCode());
        }
    }
    
    /**
     * clean threadLocal help gc.
     */
    public void remove() {
        HmilyTransactionHolder.getInstance().remove();
    }
    
    private HmilyParticipant filterStartHmilyParticipant(final HmilyTransaction currentTransaction) {
        final List<HmilyParticipant> hmilyParticipants = currentTransaction.getHmilyParticipants();
        return filterStartHmilyParticipant(hmilyParticipants);
    }
    
    private HmilyParticipant filterStartHmilyParticipant(final List<HmilyParticipant> hmilyParticipants) {
        if (CollectionUtils.isNotEmpty(hmilyParticipants)) {
            return hmilyParticipants.stream().filter(e -> e.getRole() == HmilyRoleEnum.START.getCode()).findFirst().orElse(null);
        }
        return null;
    }
    
    private HmilyParticipant filterSelfHmilyParticipant(List<HmilyParticipant> hmilyParticipants) {
        if (CollectionUtils.isNotEmpty(hmilyParticipants)) {
            return hmilyParticipants.stream().filter(e -> e.getParticipantRefId() != null).findFirst().orElse(null);
        }
        return null;
    }
    
    private HmilyTransaction createHmilyTransaction() {
        HmilyTransaction hmilyTransaction = new HmilyTransaction();
        hmilyTransaction.setTransId(IdWorkerUtils.getInstance().createUUID());
        hmilyTransaction.setStatus(HmilyActionEnum.PRE_TRY.getCode());
        hmilyTransaction.setTransType(TransTypeEnum.TCC.name());
        return hmilyTransaction;
    }
    
    private HmilyParticipant buildHmilyParticipant(final ProceedingJoinPoint point, final Long participantId, final Long participantRefId, final int role, final Long transId) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> clazz = point.getTarget().getClass();
        Object[] args = point.getArgs();
        final HmilyTCC hmilyTCC = method.getAnnotation(HmilyTCC.class);
        String confirmMethodName = hmilyTCC.confirmMethod();
        String cancelMethodName = hmilyTCC.cancelMethod();
        if (StringUtils.isBlank(confirmMethodName) || StringUtils.isBlank(cancelMethodName)) {
            return null;
        }
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        if (null == participantId) {
            hmilyParticipant.setParticipantId(IdWorkerUtils.getInstance().createUUID());
        } else {
            hmilyParticipant.setParticipantId(participantId);
        }
        if (null != participantRefId) {
            hmilyParticipant.setParticipantRefId(participantRefId);
        }
        hmilyParticipant.setTransId(transId);
        hmilyParticipant.setTransType(TransTypeEnum.TCC.name());
        hmilyParticipant.setStatus(HmilyActionEnum.PRE_TRY.getCode());
        hmilyParticipant.setRole(role);
        hmilyParticipant.setTargetClass(clazz.getName());
        hmilyParticipant.setTargetMethod(method.getName());
        if (StringUtils.isNoneBlank(confirmMethodName)) {
            hmilyParticipant.setConfirmMethod(confirmMethodName);
            HmilyInvocation confirmInvocation = new HmilyInvocation(clazz.getInterfaces()[0], method.getName(), method.getParameterTypes(), args);
            hmilyParticipant.setConfirmHmilyInvocation(confirmInvocation);
        }
        if (StringUtils.isNoneBlank(cancelMethodName)) {
            hmilyParticipant.setCancelMethod(cancelMethodName);
            HmilyInvocation cancelInvocation = new HmilyInvocation(clazz.getInterfaces()[0], method.getName(), method.getParameterTypes(), args);
            hmilyParticipant.setCancelHmilyInvocation(cancelInvocation);
        }
        return hmilyParticipant;
    }
}
