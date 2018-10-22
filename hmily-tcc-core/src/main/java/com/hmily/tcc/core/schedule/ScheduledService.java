/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.hmily.tcc.core.schedule;

import com.google.common.collect.Lists;
import com.hmily.tcc.annotation.TccPatternEnum;
import com.hmily.tcc.common.bean.context.TccTransactionContext;
import com.hmily.tcc.common.bean.entity.Participant;
import com.hmily.tcc.common.bean.entity.TccInvocation;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.config.TccConfig;
import com.hmily.tcc.common.enums.TccActionEnum;
import com.hmily.tcc.common.enums.TccRoleEnum;
import com.hmily.tcc.common.utils.LogUtil;
import com.hmily.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.hmily.tcc.core.concurrent.threadpool.HmilyThreadFactory;
import com.hmily.tcc.core.helper.SpringBeanUtils;
import com.hmily.tcc.core.spi.CoordinatorRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * this is scheduled execute transaction log.
 *
 * @author xiaoyu(Myth)
 */
public class ScheduledService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledService.class);

    private ScheduledExecutorService scheduledExecutorService;

    private TccConfig tccConfig;

    private CoordinatorRepository coordinatorRepository;

    public ScheduledService(final TccConfig tccConfig, final CoordinatorRepository coordinatorRepository) {
        this.tccConfig = tccConfig;
        this.coordinatorRepository = coordinatorRepository;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1, HmilyThreadFactory.create("tccRollBackService", true));
    }

    /**
     * if have some exception by schedule execute tcc transaction log.
     */
    public void scheduledRollBack() {
        scheduledExecutorService
                .scheduleWithFixedDelay(() -> {
                    LogUtil.info(LOGGER, "rollback execute delayTime:{}", () -> tccConfig.getScheduledDelay());
                    try {
                        final List<TccTransaction> tccTransactions = coordinatorRepository.listAllByDelay(acquireData());
                        if (CollectionUtils.isEmpty(tccTransactions)) {
                            return;
                        }
                        for (TccTransaction tccTransaction : tccTransactions) {
                            // if the try is not completed, no compensation will be provided (to prevent various exceptions in the try phase)
                            if (tccTransaction.getRole() == TccRoleEnum.PROVIDER.getCode() && tccTransaction.getStatus() == TccActionEnum.PRE_TRY.getCode()) {
                                coordinatorRepository.remove(tccTransaction.getTransId());
                                continue;
                            }
                            if (tccTransaction.getRetriedCount() > tccConfig.getRetryMax()) {
                                LogUtil.error(LOGGER, "This transaction exceeds the maximum number of retries and no retries will occur：{}", () -> tccTransaction);
                                continue;
                            }
                            if (Objects.equals(tccTransaction.getPattern(), TccPatternEnum.CC.getCode())
                                    && tccTransaction.getStatus() == TccActionEnum.TRYING.getCode()) {
                                continue;
                            }
                            // if the transaction role is the provider, and the number of retries in the scope class cannot be executed, only by the initiator
                            if (tccTransaction.getRole() == TccRoleEnum.PROVIDER.getCode()
                                    && (tccTransaction.getCreateTime().getTime()
                                    + tccConfig.getRecoverDelayTime() * tccConfig.getLoadFactor() * 1000 > System.currentTimeMillis())) {
                                continue;
                            }
                            try {
                                tccTransaction.setRetriedCount(tccTransaction.getRetriedCount() + 1);
                                final int rows = coordinatorRepository.update(tccTransaction);
                                // determine that rows>0 is executed to prevent concurrency when the business side is in cluster mode
                                if (rows > 0) {
                                    if (tccTransaction.getStatus() == TccActionEnum.TRYING.getCode()
                                            || tccTransaction.getStatus() == TccActionEnum.PRE_TRY.getCode()
                                            || tccTransaction.getStatus() == TccActionEnum.CANCELING.getCode()) {
                                        cancel(tccTransaction);
                                    } else if (tccTransaction.getStatus() == TccActionEnum.CONFIRMING.getCode()) {
                                        confirm(tccTransaction);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                LogUtil.error(LOGGER, "execute recover exception:{}", e::getMessage);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 30, tccConfig.getScheduledDelay(), TimeUnit.SECONDS);

    }

    private void cancel(final TccTransaction tccTransaction) {
        final List<Participant> participants = tccTransaction.getParticipants();
        List<Participant> failList = Lists.newArrayListWithCapacity(participants.size());
        boolean success = true;
        if (CollectionUtils.isNotEmpty(participants)) {
            for (Participant participant : participants) {
                try {
                    TccTransactionContext context = new TccTransactionContext();
                    context.setAction(TccActionEnum.CANCELING.getCode());
                    context.setTransId(tccTransaction.getTransId());
                    context.setRole(TccRoleEnum.START.getCode());
                    TransactionContextLocal.getInstance().set(context);
                    executeCoordinator(participant.getCancelTccInvocation());
                } catch (Exception e) {
                    LogUtil.error(LOGGER, "execute cancel exception:{}", () -> e);
                    success = false;
                    failList.add(participant);
                } finally {
                    TransactionContextLocal.getInstance().remove();
                }
            }
            executeHandler(success, tccTransaction, failList);
        }

    }

    private void confirm(final TccTransaction tccTransaction) {
        final List<Participant> participants = tccTransaction.getParticipants();
        List<Participant> failList = Lists.newArrayListWithCapacity(participants.size());
        boolean success = true;
        if (CollectionUtils.isNotEmpty(participants)) {
            for (Participant participant : participants) {
                try {
                    TccTransactionContext context = new TccTransactionContext();
                    context.setAction(TccActionEnum.CONFIRMING.getCode());
                    context.setRole(TccRoleEnum.START.getCode());
                    context.setTransId(tccTransaction.getTransId());
                    TransactionContextLocal.getInstance().set(context);
                    executeCoordinator(participant.getConfirmTccInvocation());
                } catch (Exception e) {
                    LogUtil.error(LOGGER, "execute confirm exception:{}", () -> e);
                    success = false;
                    failList.add(participant);
                } finally {
                    TransactionContextLocal.getInstance().remove();
                }
            }
            executeHandler(success, tccTransaction, failList);
        }
    }

    private void executeHandler(final boolean success, final TccTransaction currentTransaction, final List<Participant> failList) {
        if (success) {
            coordinatorRepository.remove(currentTransaction.getTransId());
        } else {
            currentTransaction.setParticipants(failList);
            coordinatorRepository.updateParticipant(currentTransaction);
        }
    }

    @SuppressWarnings("unchecked")
    private void executeCoordinator(final TccInvocation tccInvocation) throws Exception {
        if (Objects.nonNull(tccInvocation)) {
            final Class clazz = tccInvocation.getTargetClass();
            final String method = tccInvocation.getMethodName();
            final Object[] args = tccInvocation.getArgs();
            final Class[] parameterTypes = tccInvocation.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);
            LogUtil.debug(LOGGER, "Scheduled tasks execute transaction compensation:{}", () -> tccInvocation.getTargetClass() + ":" + tccInvocation.getMethodName());
        }
    }

    private Date acquireData() {
        return new Date(LocalDateTime.now().atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli() - (tccConfig.getRecoverDelayTime() * 1000));
    }

}
