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

package com.hmily.tcc.core.service.executor;

import com.google.common.collect.Lists;
import com.hmily.tcc.annotation.Tcc;
import com.hmily.tcc.annotation.TccPatternEnum;
import com.hmily.tcc.common.bean.context.TccTransactionContext;
import com.hmily.tcc.common.bean.entity.Participant;
import com.hmily.tcc.common.bean.entity.TccInvocation;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.enums.EventTypeEnum;
import com.hmily.tcc.common.enums.TccActionEnum;
import com.hmily.tcc.common.enums.TccRoleEnum;
import com.hmily.tcc.common.exception.TccRuntimeException;
import com.hmily.tcc.common.utils.LogUtil;
import com.hmily.tcc.core.cache.TccTransactionCacheManager;
import com.hmily.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.hmily.tcc.core.disruptor.publisher.HmilyTransactionEventPublisher;
import com.hmily.tcc.core.helper.SpringBeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
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
 * this is transaction manager.
 *
 * @author xiaoyu
 */
@Component
@SuppressWarnings("unchecked")
public class HmilyTransactionExecutor {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyTransactionExecutor.class);

    /**
     * transaction save threadLocal.
     */
    private static final ThreadLocal<TccTransaction> CURRENT = new ThreadLocal<>();

    private HmilyTransactionEventPublisher hmilyTransactionEventPublisher;

    @Autowired
    public HmilyTransactionExecutor(final HmilyTransactionEventPublisher hmilyTransactionEventPublisher) {
        this.hmilyTransactionEventPublisher = hmilyTransactionEventPublisher;
    }

    /**
     * transaction begin.
     *
     * @param point cut point.
     * @return TccTransaction
     */
    public TccTransaction begin(final ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, () -> "......hmily transaction！start....");
        //build tccTransaction
        final TccTransaction tccTransaction = buildTccTransaction(point, TccRoleEnum.START.getCode(), null);
        //save tccTransaction in threadLocal
        CURRENT.set(tccTransaction);
        //publishEvent
        hmilyTransactionEventPublisher.publishEvent(tccTransaction, EventTypeEnum.SAVE.getCode());
        //set TccTransactionContext this context transfer remote
        TccTransactionContext context = new TccTransactionContext();
        //set action is try
        context.setAction(TccActionEnum.TRYING.getCode());
        context.setTransId(tccTransaction.getTransId());
        context.setRole(TccRoleEnum.START.getCode());
        TransactionContextLocal.getInstance().set(context);
        return tccTransaction;
    }


    /**
     * this is Participant transaction begin.
     *
     * @param context transaction context.
     * @param point   cut point
     * @return TccTransaction
     */
    public TccTransaction beginParticipant(final TccTransactionContext context, final ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, "...Participant hmily transaction ！start..：{}", context::toString);
        final TccTransaction tccTransaction = buildTccTransaction(point, TccRoleEnum.PROVIDER.getCode(), context.getTransId());
        //cache by guava
        TccTransactionCacheManager.getInstance().cacheTccTransaction(tccTransaction);
        //publishEvent
        hmilyTransactionEventPublisher.publishEvent(tccTransaction, EventTypeEnum.SAVE.getCode());
        //Nested transaction support
        context.setRole(TccRoleEnum.LOCAL.getCode());
        TransactionContextLocal.getInstance().set(context);
        return tccTransaction;
    }

    /**
     * update transaction status by disruptor.
     *
     * @param tccTransaction {@linkplain TccTransaction}
     */
    public void updateStatus(final TccTransaction tccTransaction) {
        hmilyTransactionEventPublisher.publishEvent(tccTransaction, EventTypeEnum.UPDATE_STATUS.getCode());
    }

    /**
     * delete transaction by disruptor.
     *
     * @param tccTransaction {@linkplain TccTransaction}
     */
    public void deleteTransaction(final TccTransaction tccTransaction) {
        hmilyTransactionEventPublisher.publishEvent(tccTransaction, EventTypeEnum.DELETE.getCode());
    }

    /**
     * update Participant in transaction by disruptor.
     *
     * @param tccTransaction {@linkplain TccTransaction}
     */
    public void updateParticipant(final TccTransaction tccTransaction) {
        hmilyTransactionEventPublisher.publishEvent(tccTransaction, EventTypeEnum.UPDATE_PARTICIPANT.getCode());
    }

    /**
     * acquired by threadLocal.
     *
     * @return {@linkplain TccTransaction}
     */
    public TccTransaction getCurrentTransaction() {
        return CURRENT.get();
    }

    /**
     * add participant.
     *
     * @param participant {@linkplain Participant}
     */
    public void enlistParticipant(final Participant participant) {
        if (Objects.isNull(participant)) {
            return;
        }
        Optional.ofNullable(getCurrentTransaction())
                .ifPresent(c -> {
                    c.registerParticipant(participant);
                    updateParticipant(c);
                });
    }

    /**
     * when nested transaction add participant.
     *
     * @param transId     key
     * @param participant {@linkplain Participant}
     */
    public void registerByNested(final String transId, final Participant participant) {
        if (Objects.isNull(participant)
                || Objects.isNull(participant.getCancelTccInvocation())
                || Objects.isNull(participant.getConfirmTccInvocation())) {
            return;
        }
        final TccTransaction tccTransaction = TccTransactionCacheManager.getInstance().getTccTransaction(transId);
        Optional.ofNullable(tccTransaction)
                .ifPresent(c -> {
                    c.registerParticipant(participant);
                    updateParticipant(c);
                });
    }

    /**
     * Call the confirm method and basically if the initiator calls here call the remote or the original method
     * However, the context sets the call confirm
     * The remote service calls the confirm method.
     *
     * @param currentTransaction {@linkplain TccTransaction}
     * @throws TccRuntimeException ex
     */
    public void confirm(final TccTransaction currentTransaction) throws TccRuntimeException {
        LogUtil.debug(LOGGER, () -> "tcc confirm .......！start");
        if (Objects.isNull(currentTransaction) || CollectionUtils.isEmpty(currentTransaction.getParticipants())) {
            return;
        }
        currentTransaction.setStatus(TccActionEnum.CONFIRMING.getCode());
        updateStatus(currentTransaction);
        final List<Participant> participants = currentTransaction.getParticipants();
        List<Participant> failList = Lists.newArrayListWithCapacity(participants.size());
        boolean success = true;
        if (CollectionUtils.isNotEmpty(participants)) {
            for (Participant participant : participants) {
                try {
                    TccTransactionContext context = new TccTransactionContext();
                    context.setAction(TccActionEnum.CONFIRMING.getCode());
                    context.setRole(TccRoleEnum.START.getCode());
                    context.setTransId(participant.getTransId());
                    TransactionContextLocal.getInstance().set(context);
                    executeParticipantMethod(participant.getConfirmTccInvocation());
                } catch (Exception e) {
                    LogUtil.error(LOGGER, "execute confirm :{}", () -> e);
                    success = false;
                    failList.add(participant);
                } finally {
                	TransactionContextLocal.getInstance().remove();
                }
            }
            executeHandler(success, currentTransaction, failList);
        }
    }

    /**
     * cancel transaction.
     *
     * @param currentTransaction {@linkplain TccTransaction}
     */
    public void cancel(final TccTransaction currentTransaction) {
        LogUtil.debug(LOGGER, () -> "tcc cancel ...........start!");
        if (Objects.isNull(currentTransaction) || CollectionUtils.isEmpty(currentTransaction.getParticipants())) {
            return;
        }
        //if cc pattern，can not execute cancel
        if (currentTransaction.getStatus() == TccActionEnum.TRYING.getCode()
                && Objects.equals(currentTransaction.getPattern(), TccPatternEnum.CC.getCode())) {
            deleteTransaction(currentTransaction);
            return;
        }
        final List<Participant> participants = filterPoint(currentTransaction);
        boolean success = true;
        List<Participant> failList = Lists.newArrayListWithCapacity(participants.size());
        if (CollectionUtils.isNotEmpty(participants)) {
            currentTransaction.setStatus(TccActionEnum.CANCELING.getCode());
            //update cancel
            updateStatus(currentTransaction);
            for (Participant participant : participants) {
                try {
                    TccTransactionContext context = new TccTransactionContext();
                    context.setAction(TccActionEnum.CANCELING.getCode());
                    context.setTransId(participant.getTransId());
                    context.setRole(TccRoleEnum.START.getCode());
                    TransactionContextLocal.getInstance().set(context);
                    executeParticipantMethod(participant.getCancelTccInvocation());
                } catch (Throwable e) {
                    LogUtil.error(LOGGER, "execute cancel ex:{}", () -> e);
                    success = false;
                    failList.add(participant);
                } finally {
                	TransactionContextLocal.getInstance().remove();
                }
            }
            executeHandler(success, currentTransaction, failList);
        }
    }

    private void executeHandler(final boolean success, final TccTransaction currentTransaction, final List<Participant> failList) {
        TccTransactionCacheManager.getInstance().removeByKey(currentTransaction.getTransId());
        if (success) {
            deleteTransaction(currentTransaction);
        } else {
            currentTransaction.setParticipants(failList);
            updateParticipant(currentTransaction);
            throw new TccRuntimeException(failList.toString());
        }
    }

    private List<Participant> filterPoint(final TccTransaction currentTransaction) {
        final List<Participant> participants = currentTransaction.getParticipants();
        if (CollectionUtils.isNotEmpty(participants)) {
            if (currentTransaction.getStatus() == TccActionEnum.TRYING.getCode()
                    && currentTransaction.getRole() == TccRoleEnum.START.getCode()) {
                return participants.stream()
                        .limit(participants.size())
                        .filter(Objects::nonNull).collect(Collectors.toList());
            }
        }
        return participants;
    }

    private void executeParticipantMethod(final TccInvocation tccInvocation) throws Exception {
        if (Objects.nonNull(tccInvocation)) {
            final Class clazz = tccInvocation.getTargetClass();
            final String method = tccInvocation.getMethodName();
            final Object[] args = tccInvocation.getArgs();
            final Class[] parameterTypes = tccInvocation.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);
        }
    }

    /**
     * clean threadLocal help gc.
     */
    public void remove() {
        CURRENT.remove();
    }

    private TccTransaction buildTccTransaction(final ProceedingJoinPoint point, final int role, final String transId) {
        TccTransaction tccTransaction;
        if (StringUtils.isNoneBlank(transId)) {
            tccTransaction = new TccTransaction(transId);
        } else {
            tccTransaction = new TccTransaction();
        }
        tccTransaction.setStatus(TccActionEnum.PRE_TRY.getCode());
        tccTransaction.setRole(role);
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> clazz = point.getTarget().getClass();
        Object[] args = point.getArgs();
        final Tcc tcc = method.getAnnotation(Tcc.class);
        final TccPatternEnum pattern = tcc.pattern();
        tccTransaction.setTargetClass(clazz.getName());
        tccTransaction.setTargetMethod(method.getName());
        tccTransaction.setPattern(pattern.getCode());
        TccInvocation confirmInvocation = null;
        String confirmMethodName = tcc.confirmMethod();
        String cancelMethodName = tcc.cancelMethod();
        if (StringUtils.isNoneBlank(confirmMethodName)) {
            tccTransaction.setConfirmMethod(confirmMethodName);
            confirmInvocation = new TccInvocation(clazz, confirmMethodName, method.getParameterTypes(), args);
        }
        TccInvocation cancelInvocation = null;
        if (StringUtils.isNoneBlank(cancelMethodName)) {
            tccTransaction.setCancelMethod(cancelMethodName);
            cancelInvocation = new TccInvocation(clazz, cancelMethodName, method.getParameterTypes(), args);
        }
        final Participant participant = new Participant(tccTransaction.getTransId(), confirmInvocation, cancelInvocation);
        tccTransaction.registerParticipant(participant);
        return tccTransaction;
    }

}
