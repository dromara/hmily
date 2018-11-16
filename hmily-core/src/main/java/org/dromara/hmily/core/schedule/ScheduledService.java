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

package org.dromara.hmily.core.schedule;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.dromara.hmily.annotation.PatternEnum;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.bean.entity.HmilyInvocation;
import org.dromara.hmily.common.bean.entity.HmilyParticipant;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.config.HmilyConfig;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.dromara.hmily.core.concurrent.threadpool.HmilyThreadFactory;
import org.dromara.hmily.core.helper.SpringBeanUtils;
import org.dromara.hmily.core.spi.HmilyCoordinatorRepository;
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

    private HmilyConfig hmilyConfig;

    private HmilyCoordinatorRepository hmilyCoordinatorRepository;

    public ScheduledService(final HmilyConfig hmilyConfig, final HmilyCoordinatorRepository hmilyCoordinatorRepository) {
        this.hmilyConfig = hmilyConfig;
        this.hmilyCoordinatorRepository = hmilyCoordinatorRepository;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1, HmilyThreadFactory.create("tccRollBackService", true));
    }

    /**
     * if have some exception by schedule execute tcc transaction log.
     */
    public void scheduledRollBack() {
        scheduledExecutorService
                .scheduleWithFixedDelay(() -> {
                    LogUtil.info(LOGGER, "rollback execute delayTime:{}", () -> hmilyConfig.getScheduledDelay());
                    try {
                        final List<HmilyTransaction> hmilyTransactions = hmilyCoordinatorRepository.listAllByDelay(acquireData());
                        if (CollectionUtils.isEmpty(hmilyTransactions)) {
                            return;
                        }
                        for (HmilyTransaction hmilyTransaction : hmilyTransactions) {
                            // if the try is not completed, no compensation will be provided (to prevent various exceptions in the try phase)
                            if (hmilyTransaction.getRole() == HmilyRoleEnum.PROVIDER.getCode() && hmilyTransaction.getStatus() == HmilyActionEnum.PRE_TRY.getCode()) {
                                hmilyCoordinatorRepository.remove(hmilyTransaction.getTransId());
                                continue;
                            }
                            if (hmilyTransaction.getRetriedCount() > hmilyConfig.getRetryMax()) {
                                LogUtil.error(LOGGER, "This transaction exceeds the maximum number of retries and no retries will occur：{}", () -> hmilyTransaction);
                                continue;
                            }
                            if (Objects.equals(hmilyTransaction.getPattern(), PatternEnum.CC.getCode())
                                    && hmilyTransaction.getStatus() == HmilyActionEnum.TRYING.getCode()) {
                                continue;
                            }
                            // if the transaction role is the provider, and the number of retries in the scope class cannot be executed, only by the initiator
                            if (hmilyTransaction.getRole() == HmilyRoleEnum.PROVIDER.getCode()
                                    && (hmilyTransaction.getCreateTime().getTime()
                                    + hmilyConfig.getRecoverDelayTime() * hmilyConfig.getLoadFactor() * 1000 > System.currentTimeMillis())) {
                                continue;
                            }
                            try {
                                hmilyTransaction.setRetriedCount(hmilyTransaction.getRetriedCount() + 1);
                                final int rows = hmilyCoordinatorRepository.update(hmilyTransaction);
                                // determine that rows>0 is executed to prevent concurrency when the business side is in cluster mode
                                if (rows > 0) {
                                    if (hmilyTransaction.getStatus() == HmilyActionEnum.TRYING.getCode()
                                            || hmilyTransaction.getStatus() == HmilyActionEnum.PRE_TRY.getCode()
                                            || hmilyTransaction.getStatus() == HmilyActionEnum.CANCELING.getCode()) {
                                        cancel(hmilyTransaction);
                                    } else if (hmilyTransaction.getStatus() == HmilyActionEnum.CONFIRMING.getCode()) {
                                        confirm(hmilyTransaction);
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
                }, 30, hmilyConfig.getScheduledDelay(), TimeUnit.SECONDS);

    }

    private void cancel(final HmilyTransaction hmilyTransaction) {
        final List<HmilyParticipant> hmilyParticipants = hmilyTransaction.getHmilyParticipants();
        List<HmilyParticipant> failList = Lists.newArrayListWithCapacity(hmilyParticipants.size());
        boolean success = true;
        if (CollectionUtils.isNotEmpty(hmilyParticipants)) {
            for (HmilyParticipant hmilyParticipant : hmilyParticipants) {
                try {
                    HmilyTransactionContext context = new HmilyTransactionContext();
                    context.setAction(HmilyActionEnum.CANCELING.getCode());
                    context.setTransId(hmilyTransaction.getTransId());
                    context.setRole(HmilyRoleEnum.START.getCode());
                    HmilyTransactionContextLocal.getInstance().set(context);
                    executeCoordinator(hmilyParticipant.getCancelHmilyInvocation());
                } catch (Exception e) {
                    LogUtil.error(LOGGER, "execute cancel exception:{}", () -> e);
                    success = false;
                    failList.add(hmilyParticipant);
                } finally {
                    HmilyTransactionContextLocal.getInstance().remove();
                }
            }
            executeHandler(success, hmilyTransaction, failList);
        }

    }

    private void confirm(final HmilyTransaction hmilyTransaction) {
        final List<HmilyParticipant> hmilyParticipants = hmilyTransaction.getHmilyParticipants();
        List<HmilyParticipant> failList = Lists.newArrayListWithCapacity(hmilyParticipants.size());
        boolean success = true;
        if (CollectionUtils.isNotEmpty(hmilyParticipants)) {
            for (HmilyParticipant hmilyParticipant : hmilyParticipants) {
                try {
                    HmilyTransactionContext context = new HmilyTransactionContext();
                    context.setAction(HmilyActionEnum.CONFIRMING.getCode());
                    context.setRole(HmilyRoleEnum.START.getCode());
                    context.setTransId(hmilyTransaction.getTransId());
                    HmilyTransactionContextLocal.getInstance().set(context);
                    executeCoordinator(hmilyParticipant.getConfirmHmilyInvocation());
                } catch (Exception e) {
                    LogUtil.error(LOGGER, "execute confirm exception:{}", () -> e);
                    success = false;
                    failList.add(hmilyParticipant);
                } finally {
                    HmilyTransactionContextLocal.getInstance().remove();
                }
            }
            executeHandler(success, hmilyTransaction, failList);
        }
    }

    private void executeHandler(final boolean success, final HmilyTransaction currentTransaction, final List<HmilyParticipant> failList) {
        if (success) {
            hmilyCoordinatorRepository.remove(currentTransaction.getTransId());
        } else {
            currentTransaction.setHmilyParticipants(failList);
            hmilyCoordinatorRepository.updateParticipant(currentTransaction);
        }
    }

    @SuppressWarnings("unchecked")
    private void executeCoordinator(final HmilyInvocation hmilyInvocation) throws Exception {
        if (Objects.nonNull(hmilyInvocation)) {
            final Class clazz = hmilyInvocation.getTargetClass();
            final String method = hmilyInvocation.getMethodName();
            final Object[] args = hmilyInvocation.getArgs();
            final Class[] parameterTypes = hmilyInvocation.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);
            LogUtil.debug(LOGGER, "Scheduled tasks execute transaction compensation:{}", () -> hmilyInvocation.getTargetClass() + ":" + hmilyInvocation.getMethodName());
        }
    }

    private Date acquireData() {
        return new Date(LocalDateTime.now().atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli() - (hmilyConfig.getRecoverDelayTime() * 1000));
    }

}
