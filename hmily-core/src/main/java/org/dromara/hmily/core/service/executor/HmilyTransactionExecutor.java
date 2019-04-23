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

package org.dromara.hmily.core.service.executor;

import com.google.common.collect.Lists;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.annotation.PatternEnum;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.bean.entity.HmilyInvocation;
import org.dromara.hmily.common.bean.entity.HmilyParticipant;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.enums.EventTypeEnum;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.core.cache.HmilyTransactionGuavaCacheManager;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.dromara.hmily.core.disruptor.publisher.HmilyTransactionEventPublisher;
import org.dromara.hmily.core.reflect.HmilyReflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * this is hmily transaction manager.
 *
 * @author xiaoyu
 */
@Component
public class HmilyTransactionExecutor {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyTransactionExecutor.class);

    /**
     * transaction save threadLocal.
     */
    private static final ThreadLocal<HmilyTransaction> CURRENT = new ThreadLocal<>();

    private final HmilyTransactionEventPublisher hmilyTransactionEventPublisher;

    /**
     * Instantiates a new Hmily transaction executor.
     *
     * @param hmilyTransactionEventPublisher the hmily transaction event publisher
     */
    @Autowired
    public HmilyTransactionExecutor(final HmilyTransactionEventPublisher hmilyTransactionEventPublisher) {
        this.hmilyTransactionEventPublisher = hmilyTransactionEventPublisher;
    }

    /**
     * transaction preTry.
     *
     * @param point cut point.
     * @return TccTransaction hmily transaction
     */
    public HmilyTransaction preTry(final ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, () -> "......hmily transaction starter....");
        //build tccTransaction
        final HmilyTransaction hmilyTransaction = buildHmilyTransaction(point, HmilyRoleEnum.START.getCode(), null);
        //save tccTransaction in threadLocal
        CURRENT.set(hmilyTransaction);
        //publishEvent
        hmilyTransactionEventPublisher.publishEvent(hmilyTransaction, EventTypeEnum.SAVE.getCode());
        //set TccTransactionContext this context transfer remote
        HmilyTransactionContext context = new HmilyTransactionContext();
        //set action is try
        context.setAction(HmilyActionEnum.TRYING.getCode());
        context.setTransId(hmilyTransaction.getTransId());
        context.setRole(HmilyRoleEnum.START.getCode());
        HmilyTransactionContextLocal.getInstance().set(context);
        return hmilyTransaction;
    }

    /**
     * this is Participant transaction preTry.
     *
     * @param context transaction context.
     * @param point   cut point
     * @return TccTransaction hmily transaction
     */
    public HmilyTransaction preTryParticipant(final HmilyTransactionContext context, final ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, "participant hmily transaction start..：{}", context::toString);
        final HmilyTransaction hmilyTransaction = buildHmilyTransaction(point, HmilyRoleEnum.PROVIDER.getCode(), context.getTransId());
        //cache by guava
        HmilyTransactionGuavaCacheManager.getInstance().cacheHmilyTransaction(hmilyTransaction);
        //publishEvent
        hmilyTransactionEventPublisher.publishEvent(hmilyTransaction, EventTypeEnum.SAVE.getCode());
        //Nested transaction support
        context.setRole(HmilyRoleEnum.LOCAL.getCode());
        HmilyTransactionContextLocal.getInstance().set(context);
        return hmilyTransaction;
    }

    /**
     * Call the confirm method and basically if the initiator calls here call the remote or the original method
     * However, the context sets the call confirm
     * The remote service calls the confirm method.
     *
     * @param currentTransaction {@linkplain HmilyTransaction}
     * @return the object
     * @throws HmilyRuntimeException ex
     */
    public Object confirm(final HmilyTransaction currentTransaction) throws HmilyRuntimeException {
        LogUtil.debug(LOGGER, () -> "hmily transaction confirm .......！start");
        if (Objects.isNull(currentTransaction) || CollectionUtils.isEmpty(currentTransaction.getHmilyParticipants())) {
            return null;
        }
        currentTransaction.setStatus(HmilyActionEnum.CONFIRMING.getCode());
        updateStatus(currentTransaction);
        final List<HmilyParticipant> hmilyParticipants = currentTransaction.getHmilyParticipants();
        boolean success = true;
        if (CollectionUtils.isNotEmpty(hmilyParticipants)) {
            List<HmilyParticipant> failList = Lists.newArrayListWithCapacity(hmilyParticipants.size());
            List<Object> results = Lists.newArrayListWithCapacity(hmilyParticipants.size());
            for (HmilyParticipant hmilyParticipant : hmilyParticipants) {
                try {
                    final Object result = HmilyReflector.executor(hmilyParticipant.getTransId(),
                            HmilyActionEnum.CONFIRMING,
                            hmilyParticipant.getConfirmHmilyInvocation());
                    results.add(result);
                } catch (Exception e) {
                    LogUtil.error(LOGGER, "execute confirm :{}", () -> e);
                    success = false;
                    failList.add(hmilyParticipant);
                } finally {
                    HmilyTransactionContextLocal.getInstance().remove();
                }
            }
            executeHandler(success, currentTransaction, failList);
            return results.get(0);
        }
        return null;
    }

    /**
     * cancel transaction.
     *
     * @param currentTransaction {@linkplain HmilyTransaction}
     * @return the object
     */
    public Object cancel(final HmilyTransaction currentTransaction) {
        LogUtil.debug(LOGGER, () -> "tcc cancel ...........start!");
        if (Objects.isNull(currentTransaction) || CollectionUtils.isEmpty(currentTransaction.getHmilyParticipants())) {
            return null;
        }
        //if cc pattern，can not execute cancel
        if (currentTransaction.getStatus() == HmilyActionEnum.TRYING.getCode()
                && Objects.equals(currentTransaction.getPattern(), PatternEnum.CC.getCode())) {
            deleteTransaction(currentTransaction);
            return null;
        }
        currentTransaction.setStatus(HmilyActionEnum.CANCELING.getCode());
        //update cancel
        updateStatus(currentTransaction);
        final List<HmilyParticipant> hmilyParticipants = filterPoint(currentTransaction);
        boolean success = true;
        if (CollectionUtils.isNotEmpty(hmilyParticipants)) {
            List<HmilyParticipant> failList = Lists.newArrayListWithCapacity(hmilyParticipants.size());
            List<Object> results = Lists.newArrayListWithCapacity(hmilyParticipants.size());
            for (HmilyParticipant hmilyParticipant : hmilyParticipants) {
                try {
                    final Object result = HmilyReflector.executor(hmilyParticipant.getTransId(),
                            HmilyActionEnum.CANCELING,
                            hmilyParticipant.getCancelHmilyInvocation());
                    results.add(result);
                } catch (Exception e) {
                    LogUtil.error(LOGGER, "execute cancel ex:{}", () -> e);
                    success = false;
                    failList.add(hmilyParticipant);
                } finally {
                    HmilyTransactionContextLocal.getInstance().remove();
                }
            }
            executeHandler(success, currentTransaction, failList);
            return results.get(0);
        }
        return null;
    }

    /**
     * update transaction status by disruptor.
     *
     * @param hmilyTransaction {@linkplain HmilyTransaction}
     */
    public void updateStatus(final HmilyTransaction hmilyTransaction) {
        hmilyTransactionEventPublisher.publishEvent(hmilyTransaction, EventTypeEnum.UPDATE_STATUS.getCode());
    }

    /**
     * delete transaction by disruptor.
     *
     * @param hmilyTransaction {@linkplain HmilyTransaction}
     */
    public void deleteTransaction(final HmilyTransaction hmilyTransaction) {
        hmilyTransactionEventPublisher.publishEvent(hmilyTransaction, EventTypeEnum.DELETE.getCode());
    }

    /**
     * update Participant in transaction by disruptor.
     *
     * @param hmilyTransaction {@linkplain HmilyTransaction}
     */
    public void updateParticipant(final HmilyTransaction hmilyTransaction) {
        hmilyTransactionEventPublisher.publishEvent(hmilyTransaction, EventTypeEnum.UPDATE_PARTICIPANT.getCode());
    }

    /**
     * acquired by threadLocal.
     *
     * @return {@linkplain HmilyTransaction}
     */
    public HmilyTransaction getCurrentTransaction() {
        return CURRENT.get();
    }


    /**
     * clean threadLocal help gc.
     */
    public void remove() {
        CURRENT.remove();
    }

    /**
     * add participant.
     *
     * @param hmilyParticipant {@linkplain HmilyParticipant}
     */
    public void enlistParticipant(final HmilyParticipant hmilyParticipant) {
        if (Objects.isNull(hmilyParticipant)) {
            return;
        }
        Optional.ofNullable(getCurrentTransaction())
                .ifPresent(c -> {
                    c.registerParticipant(hmilyParticipant);
                    updateParticipant(c);
                });
    }

    /**
     * when nested transaction add participant.
     *
     * @param transId          key
     * @param hmilyParticipant {@linkplain HmilyParticipant}
     */
    public void registerByNested(final String transId, final HmilyParticipant hmilyParticipant) {
        if (Objects.isNull(hmilyParticipant)
                || Objects.isNull(hmilyParticipant.getCancelHmilyInvocation())
                || Objects.isNull(hmilyParticipant.getConfirmHmilyInvocation())) {
            return;
        }
        final HmilyTransaction hmilyTransaction =
                HmilyTransactionGuavaCacheManager.getInstance().getHmilyTransaction(transId);
        Optional.ofNullable(hmilyTransaction)
                .ifPresent(transaction -> {
                    transaction.registerParticipant(hmilyParticipant);
                    updateParticipant(transaction);
                });
    }

    private void executeHandler(final boolean success, final HmilyTransaction currentTransaction, final List<HmilyParticipant> failList) {
        HmilyTransactionGuavaCacheManager.getInstance().removeByKey(currentTransaction.getTransId());
        if (success) {
            deleteTransaction(currentTransaction);
        } else {
            currentTransaction.setHmilyParticipants(failList);
            updateParticipant(currentTransaction);
            throw new HmilyRuntimeException(failList.toString());
        }
    }

    private List<HmilyParticipant> filterPoint(final HmilyTransaction currentTransaction) {
        final List<HmilyParticipant> hmilyParticipants = currentTransaction.getHmilyParticipants();
        if (CollectionUtils.isNotEmpty(hmilyParticipants)) {
            if (currentTransaction.getStatus() == HmilyActionEnum.TRYING.getCode()
                    && currentTransaction.getRole() == HmilyRoleEnum.START.getCode()) {
                return hmilyParticipants.stream()
                        .limit(hmilyParticipants.size())
                        .filter(Objects::nonNull).collect(Collectors.toList());
            }
        }
        return hmilyParticipants;
    }

    private HmilyTransaction buildHmilyTransaction(final ProceedingJoinPoint point, final int role, final String transId) {
        HmilyTransaction hmilyTransaction;
        if (StringUtils.isNoneBlank(transId)) {
            hmilyTransaction = new HmilyTransaction(transId);
        } else {
            hmilyTransaction = new HmilyTransaction();
        }
        hmilyTransaction.setStatus(HmilyActionEnum.PRE_TRY.getCode());
        hmilyTransaction.setRole(role);
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> clazz = point.getTarget().getClass();
        Object[] args = point.getArgs();
        final Hmily hmily = method.getAnnotation(Hmily.class);
        final PatternEnum pattern = hmily.pattern();
        hmilyTransaction.setTargetClass(clazz.getName());
        hmilyTransaction.setTargetMethod(method.getName());
        hmilyTransaction.setPattern(pattern.getCode());
        HmilyInvocation confirmInvocation = null;
        String confirmMethodName = hmily.confirmMethod();
        String cancelMethodName = hmily.cancelMethod();
        if (StringUtils.isNoneBlank(confirmMethodName)) {
            hmilyTransaction.setConfirmMethod(confirmMethodName);
            confirmInvocation = new HmilyInvocation(clazz, confirmMethodName, method.getParameterTypes(), args);
        }
        HmilyInvocation cancelInvocation = null;
        if (StringUtils.isNoneBlank(cancelMethodName)) {
            hmilyTransaction.setCancelMethod(cancelMethodName);
            cancelInvocation = new HmilyInvocation(clazz, cancelMethodName, method.getParameterTypes(), args);
        }
        final HmilyParticipant hmilyParticipant = new HmilyParticipant(hmilyTransaction.getTransId(), confirmInvocation, cancelInvocation);
        hmilyTransaction.registerParticipant(hmilyParticipant);
        return hmilyTransaction;
    }

}
