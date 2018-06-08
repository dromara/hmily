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
import java.util.stream.Collectors;


/**
 * this is transaction manager.
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
     * @param point cut point.
     * @return TccTransaction
     */
    public TccTransaction begin(final ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, () -> "开始执行tcc事务！start");
        //构建事务对象
        final TccTransaction tccTransaction = buildTccTransaction(point, TccRoleEnum.START.getCode(), null);
        //将事务对象保存在threadLocal中
        CURRENT.set(tccTransaction);
        //发布事务保存事件，异步保存
        hmilyTransactionEventPublisher.publishEvent(tccTransaction, EventTypeEnum.SAVE.getCode());
        //设置tcc事务上下文，这个类会传递给远端
        TccTransactionContext context = new TccTransactionContext();
        //设置执行动作为try
        context.setAction(TccActionEnum.TRYING.getCode());
        //设置事务id
        context.setTransId(tccTransaction.getTransId());
        TransactionContextLocal.getInstance().set(context);
        return tccTransaction;
    }


    /**
     * this is Participant transaction begin.
     *
     * @param context  transaction context.
     * @param point   cut point
     * @return TccTransaction
     */
    public TccTransaction beginParticipant(final TccTransactionContext context, final ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, "参与方开始执行tcc事务！start：{}", context::toString);
        final TccTransaction tccTransaction = buildTccTransaction(point, TccRoleEnum.PROVIDER.getCode(), context.getTransId());
        //提供者事务存储到guava
        TccTransactionCacheManager.getInstance().cacheTccTransaction(tccTransaction);
        //发布事务保存事件，异步保存
        hmilyTransactionEventPublisher.publishEvent(tccTransaction, EventTypeEnum.SAVE.getCode());
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
     * @param tccTransaction  {@linkplain TccTransaction}
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
        final TccTransaction currentTransaction = this.getCurrentTransaction();
        currentTransaction.registerParticipant(participant);
        updateParticipant(currentTransaction);
    }

    /**
     * 调用confirm方法 这里主要如果是发起者调用 这里调用远端的还是原来的方法
     * 不过上下文设置了调用confirm
     * 那么远端的服务则会调用confirm方法.
     *
     * @param currentTransaction {@linkplain TccTransaction}
     * @throws TccRuntimeException 异常
     */
    public void confirm(final TccTransaction currentTransaction) throws TccRuntimeException {
        LogUtil.debug(LOGGER, () -> "开始执行tcc confirm 方法！start");
        if (Objects.isNull(currentTransaction) || CollectionUtils.isEmpty(currentTransaction.getParticipants())) {
            return;
        }
        currentTransaction.setStatus(TccActionEnum.CONFIRMING.getCode());
        //更新事务日志状态 为confirm
        updateStatus(currentTransaction);
        final List<Participant> participants = currentTransaction.getParticipants();
        List<Participant> failList = Lists.newArrayListWithCapacity(participants.size());
        boolean success = true;
        if (CollectionUtils.isNotEmpty(participants)) {
            for (Participant participant : participants) {
                try {
                    TccTransactionContext context = new TccTransactionContext();
                    context.setAction(TccActionEnum.CONFIRMING.getCode());
                    context.setTransId(participant.getTransId());
                    TransactionContextLocal.getInstance().set(context);
                    executeParticipantMethod(participant.getConfirmTccInvocation());
                } catch (Exception e) {
                    LogUtil.error(LOGGER, "执行confirm方法异常:{}", () -> e);
                    success = false;
                    failList.add(participant);
                }
            }
            executeHandler(success, currentTransaction, failList);
        }
    }

    /**
     * cancel transaction.
     * @param currentTransaction {@linkplain TccTransaction}
     */
    public void cancel(final TccTransaction currentTransaction) {
        LogUtil.debug(LOGGER, () -> "开始执行tcc cancel 方法！start");
        if (Objects.isNull(currentTransaction) || CollectionUtils.isEmpty(currentTransaction.getParticipants())) {
            return;
        }
        //如果是cc模式，那么在try阶段是不会进行cancel补偿
        if (currentTransaction.getStatus() == TccActionEnum.TRYING.getCode()
                && Objects.equals(currentTransaction.getPattern(), TccPatternEnum.CC.getCode())) {
            deleteTransaction(currentTransaction);
            return;
        }
        //获取回滚节点
        final List<Participant> participants = filterPoint(currentTransaction);
        currentTransaction.setStatus(TccActionEnum.CANCELING.getCode());
        //更新事务日志状态 为cancel
        updateStatus(currentTransaction);
        boolean success = true;
        List<Participant> failList = Lists.newArrayListWithCapacity(participants.size());
        if (CollectionUtils.isNotEmpty(participants)) {
            for (Participant participant : participants) {
                try {
                    TccTransactionContext context = new TccTransactionContext();
                    context.setAction(TccActionEnum.CANCELING.getCode());
                    context.setTransId(participant.getTransId());
                    TransactionContextLocal.getInstance().set(context);
                    executeParticipantMethod(participant.getCancelTccInvocation());
                } catch (Exception e) {
                    LogUtil.error(LOGGER, "执行cancel方法异常:{}", () -> e);
                    success = false;
                    failList.add(participant);
                }
            }
            executeHandler(success, currentTransaction, failList);
        }
    }

    private void executeHandler(final boolean success, final TccTransaction currentTransaction, final List<Participant> failList) {
        if (success) {
            TransactionContextLocal.getInstance().remove();
            TccTransactionCacheManager.getInstance().removeByKey(currentTransaction.getTransId());
            deleteTransaction(currentTransaction);
        } else {
            //获取还没执行的，或者执行失败的
            currentTransaction.setParticipants(failList);
            updateParticipant(currentTransaction);
            throw new TccRuntimeException(failList.toString());
        }
    }

    private List<Participant> filterPoint(final TccTransaction currentTransaction) {
        final List<Participant> participants = currentTransaction.getParticipants();
        if (CollectionUtils.isNotEmpty(participants)) {
            //只有在发起者并且是try阶段的时候，才从上一个点开始回滚
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
     * jude transaction is running.
     * @return true
     */
    public boolean isBegin() {
        return CURRENT.get() != null;
    }

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
        //设置模式
        final TccPatternEnum pattern = tcc.pattern();
        tccTransaction.setTargetClass(clazz.getName());
        tccTransaction.setTargetMethod(method.getName());
        tccTransaction.setPattern(pattern.getCode());
        TccInvocation confirmInvocation = null;
        //获取协调方法
        String confirmMethodName = tcc.confirmMethod();
        String cancelMethodName = tcc.cancelMethod();
        if (StringUtils.isNoneBlank(confirmMethodName)) {
            confirmInvocation = new TccInvocation(clazz, confirmMethodName, method.getParameterTypes(), args);
        }
        TccInvocation cancelInvocation = null;
        if (StringUtils.isNoneBlank(cancelMethodName)) {
            cancelInvocation = new TccInvocation(clazz, cancelMethodName, method.getParameterTypes(), args);
        }
        //封装调用点
        final Participant participant = new Participant(tccTransaction.getTransId(), confirmInvocation, cancelInvocation);
        tccTransaction.registerParticipant(participant);
        return tccTransaction;
    }

}
