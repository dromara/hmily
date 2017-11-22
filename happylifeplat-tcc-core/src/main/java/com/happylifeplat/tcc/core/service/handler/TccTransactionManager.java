/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.happylifeplat.tcc.core.service.handler;


import com.google.common.collect.Lists;
import com.happylifeplat.tcc.annotation.TccPatternEnum;
import com.happylifeplat.tcc.common.enums.CoordinatorActionEnum;
import com.happylifeplat.tcc.common.enums.TccActionEnum;
import com.happylifeplat.tcc.common.enums.TccRoleEnum;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;
import com.happylifeplat.tcc.common.utils.LogUtil;
import com.happylifeplat.tcc.common.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.common.bean.entity.Participant;
import com.happylifeplat.tcc.common.bean.entity.TccInvocation;
import com.happylifeplat.tcc.common.bean.entity.TccTransaction;
import com.happylifeplat.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.happylifeplat.tcc.core.coordinator.CoordinatorService;
import com.happylifeplat.tcc.core.coordinator.command.CoordinatorAction;
import com.happylifeplat.tcc.core.coordinator.command.CoordinatorCommand;
import com.happylifeplat.tcc.core.helper.SpringBeanUtils;
import com.happylifeplat.tcc.core.service.rollback.AsyncRollbackServiceImpl;
import org.apache.commons.collections.CollectionUtils;
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
 * @author xiaoyu
 */
@Component
@SuppressWarnings("unchecked")
public class TccTransactionManager {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TccTransactionManager.class);


    /**
     * 将事务信息存放在threadLocal里面
     */
    private static final ThreadLocal<TccTransaction> CURRENT = new ThreadLocal<>();

    private final CoordinatorService coordinatorService;


    private final CoordinatorCommand coordinatorCommand;

    private final AsyncRollbackServiceImpl asyncRollbackService;

    @Autowired
    public TccTransactionManager(CoordinatorCommand coordinatorCommand, CoordinatorService coordinatorService, AsyncRollbackServiceImpl asyncRollbackService) {
        this.coordinatorCommand = coordinatorCommand;
        this.coordinatorService = coordinatorService;
        this.asyncRollbackService = asyncRollbackService;
    }


    /**
     * 该方法为发起方第一次调用
     * 也是tcc事务的入口
     */
    TccTransaction begin(ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, () -> "开始执行tcc事务！start");
        TccTransaction tccTransaction = CURRENT.get();
        if (Objects.isNull(tccTransaction)) {

            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();

            Class<?> clazz = point.getTarget().getClass();

            tccTransaction = new TccTransaction();
            tccTransaction.setStatus(TccActionEnum.PRE_TRY.getCode());
            tccTransaction.setRole(TccRoleEnum.START.getCode());
            tccTransaction.setTargetClass(clazz.getName());
            tccTransaction.setTargetMethod(method.getName());
        }
        //保存当前事务信息
        coordinatorCommand.execute(new CoordinatorAction(CoordinatorActionEnum.SAVE, tccTransaction));

        CURRENT.set(tccTransaction);

        //设置tcc事务上下文，这个类会传递给远端
        TccTransactionContext context = new TccTransactionContext();
        //设置执行动作为try
        context.setAction(TccActionEnum.TRYING.getCode());
        //设置事务id
        context.setTransId(tccTransaction.getTransId());
        TransactionContextLocal.getInstance().set(context);

        return tccTransaction;

    }

    TccTransaction providerBegin(TccTransactionContext context, ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, "参与方开始执行tcc事务！start：{}", context::toString);

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        Class<?> clazz = point.getTarget().getClass();
        TccTransaction transaction = new TccTransaction(context.getTransId());
        //设置角色为提供者
        transaction.setRole(TccRoleEnum.PROVIDER.getCode());
        transaction.setStatus(TccActionEnum.PRE_TRY.getCode());

        transaction.setTargetClass(clazz.getName());
        transaction.setTargetMethod(method.getName());
        //保存当前事务信息
        coordinatorService.save(transaction);
        //传入当前threadLocal
        CURRENT.set(transaction);
        return transaction;
    }

    TccTransaction acquire(TccTransactionContext context) {
        final TccTransaction tccTransaction = coordinatorService.findByTransId(context.getTransId());
        CURRENT.set(tccTransaction);
        return tccTransaction;
    }


    /**
     * 调用回滚接口
     */
    void cancel() {
        LogUtil.debug(LOGGER, () -> "开始执行tcc cancel 方法！start");

        final TccTransaction currentTransaction = getCurrentTransaction();
        if (Objects.isNull(currentTransaction)) {
            return;
        }

        //如果是cc模式，那么在try阶段是不会进行cancel补偿
        if (currentTransaction.getStatus() == TccActionEnum.TRYING.getCode() &&
                Objects.equals(currentTransaction.getPattern(), TccPatternEnum.CC.getCode())) {
            coordinatorCommand.execute(new CoordinatorAction(CoordinatorActionEnum.DELETE, currentTransaction));
            return;
        }
        //获取回滚节点
        final List<Participant> participants = filterPoint(currentTransaction);

        currentTransaction.setStatus(TccActionEnum.CANCELING.getCode());

        //先异步更新数据
        coordinatorCommand.execute(new CoordinatorAction(CoordinatorActionEnum.UPDATE, currentTransaction));
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


    /**
     * 调用confirm方法 这里主要如果是发起者调用 这里调用远端的还是原来的方法，不过上下文设置了调用confirm
     * 那么远端的服务则会调用confirm方法。。
     */
    void confirm() throws TccRuntimeException {

        LogUtil.debug(LOGGER, () -> "开始执行tcc confirm 方法！start");

        final TccTransaction currentTransaction = getCurrentTransaction();

        if (Objects.isNull(currentTransaction)) {
            return;
        }

        currentTransaction.setStatus(TccActionEnum.CONFIRMING.getCode());

        coordinatorCommand.execute(new CoordinatorAction(CoordinatorActionEnum.UPDATE, currentTransaction));

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


    private void executeHandler(boolean success, final TccTransaction currentTransaction,
                                List<Participant> failList) {
        if (success) {
            TransactionContextLocal.getInstance().remove();
            coordinatorCommand.execute(new CoordinatorAction(CoordinatorActionEnum.DELETE, currentTransaction));
        } else {
            //获取还没执行的，或者执行失败的
            currentTransaction.setParticipants(failList);
            coordinatorService.updateParticipant(currentTransaction);
            throw new TccRuntimeException(failList.toString());
        }
    }

    private List<Participant> filterPoint(TccTransaction currentTransaction) {
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

    private void executeParticipantMethod(TccInvocation tccInvocation) throws Exception {
        if (Objects.nonNull(tccInvocation)) {
            final Class clazz = tccInvocation.getTargetClass();
            final String method = tccInvocation.getMethodName();
            final Object[] args = tccInvocation.getArgs();
            final Class[] parameterTypes = tccInvocation.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);

        }
    }

    public boolean isBegin() {
        return CURRENT.get() != null;
    }


    void remove() {
        CURRENT.remove();
    }


    public TccTransaction getCurrentTransaction() {
        return CURRENT.get();
    }


    void removeTccTransaction(TccTransaction tccTransaction) {
        coordinatorService.remove(tccTransaction.getTransId());
    }


    void updateStatus(String transId, Integer status) {
        coordinatorService.updateStatus(transId, status);
    }

    public void enlistParticipant(Participant participant) {
        final TccTransaction transaction = this.getCurrentTransaction();
        transaction.registerParticipant(participant);
        coordinatorService.update(transaction);

    }
}
