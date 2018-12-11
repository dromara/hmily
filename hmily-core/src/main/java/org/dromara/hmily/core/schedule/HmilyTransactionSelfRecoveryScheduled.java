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

package org.dromara.hmily.core.schedule;

import org.apache.commons.collections.CollectionUtils;
import org.dromara.hmily.annotation.PatternEnum;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.config.HmilyConfig;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.core.concurrent.threadpool.HmilyThreadFactory;
import org.dromara.hmily.core.helper.SpringBeanUtils;
import org.dromara.hmily.core.service.recovery.HmilyTransactionRecoveryService;
import org.dromara.hmily.core.spi.HmilyCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The type Hmily transaction self recovery scheduled.
 *
 * @author xiaoyu(Myth)
 */
@Component
public class HmilyTransactionSelfRecoveryScheduled implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyTransactionSelfRecoveryScheduled.class);

    @Autowired
    private HmilyConfig hmilyConfig;

    private ScheduledExecutorService scheduledExecutorService;

    private HmilyCoordinatorRepository hmilyCoordinatorRepository;

    private HmilyTransactionRecoveryService hmilyTransactionRecoveryService;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        hmilyCoordinatorRepository = SpringBeanUtils.getInstance().getBean(HmilyCoordinatorRepository.class);
        this.scheduledExecutorService =
                new ScheduledThreadPoolExecutor(1,
                        HmilyThreadFactory.create("hmily-transaction-self-recovery", true));
        hmilyTransactionRecoveryService = new HmilyTransactionRecoveryService(hmilyCoordinatorRepository);
        selfRecovery();
    }

    /**
     * if have some exception by schedule execute hmily transaction log.
     */
    private void selfRecovery() {
        scheduledExecutorService
                .scheduleWithFixedDelay(() -> {
                    LogUtil.info(LOGGER, "self recovery execute delayTime:{}", () -> hmilyConfig.getScheduledDelay());
                    try {
                        final List<HmilyTransaction> hmilyTransactions = hmilyCoordinatorRepository.listAllByDelay(acquireData());
                        if (CollectionUtils.isEmpty(hmilyTransactions)) {
                            return;
                        }
                        for (HmilyTransaction hmilyTransaction : hmilyTransactions) {
                            // if the try is not completed, no compensation will be provided (to prevent various exceptions in the try phase)
                            if (hmilyTransaction.getRole() == HmilyRoleEnum.PROVIDER.getCode()
                                    && hmilyTransaction.getStatus() == HmilyActionEnum.PRE_TRY.getCode()) {
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
                                    + hmilyConfig.getRecoverDelayTime() * hmilyConfig.getLoadFactor() * 1000
                                    > System.currentTimeMillis())) {
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
                                        hmilyTransactionRecoveryService.cancel(hmilyTransaction);
                                    } else if (hmilyTransaction.getStatus() == HmilyActionEnum.CONFIRMING.getCode()) {
                                        hmilyTransactionRecoveryService.confirm(hmilyTransaction);
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
                }, hmilyConfig.getScheduledInitDelay(), hmilyConfig.getScheduledDelay(), TimeUnit.SECONDS);

    }

    private Date acquireData() {
        return new Date(LocalDateTime.now().atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli() - (hmilyConfig.getRecoverDelayTime() * 1000));
    }

}
