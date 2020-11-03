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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.common.concurrent.HmilyThreadFactory;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.core.hook.UndoHook;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.spi.ExtensionLoaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Hmily transaction self recovery scheduled.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyTransactionSelfRecoveryScheduled implements AutoCloseable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyTransactionSelfRecoveryScheduled.class);
    
    private final HmilyConfig hmilyConfig = ConfigEnv.getInstance().getConfig(HmilyConfig.class);
    
    private final HmilyRepository hmilyRepository;
    
    private final ScheduledExecutorService selfTccRecoveryExecutor;
    
    private final ScheduledExecutorService cleanHmilyTransactionExecutor;
    
    private final ScheduledExecutorService selfTacRecoveryExecutor;
    
    private ScheduledExecutorService phyDeletedExecutor;
    
    private final HmilyTransactionRecoveryService hmilyTransactionRecoveryService;
    
    public HmilyTransactionSelfRecoveryScheduled() {
        hmilyRepository = ExtensionLoaderFactory.load(HmilyRepository.class, hmilyConfig.getRepository());
        this.selfTccRecoveryExecutor = new ScheduledThreadPoolExecutor(1, HmilyThreadFactory.create("hmily-tcc-self-recovery", true));
        this.selfTacRecoveryExecutor = new ScheduledThreadPoolExecutor(1, HmilyThreadFactory.create("hmily-tac-self-recovery", true));
        this.cleanHmilyTransactionExecutor = new ScheduledThreadPoolExecutor(1, HmilyThreadFactory.create("hmily-transaction-clean", true));
        hmilyTransactionRecoveryService = new HmilyTransactionRecoveryService();
        selfTccRecovery();
        selfTacRecovery();
        cleanHmilyTransaction();
        phyDeleted();
    }
    
    private void phyDeleted() {
        if (!hmilyConfig.isPhyDeleted()) {
            int seconds = hmilyConfig.getStoreDays() * 24 * 60 * 60;
            phyDeletedExecutor = new ScheduledThreadPoolExecutor(1, HmilyThreadFactory.create("hmily-phyDeleted-clean", true));
            phyDeletedExecutor
                    .scheduleWithFixedDelay(() -> {
                        try {
                            hmilyRepository.removeHmilyTransactionByDate(acquireDelayData(seconds));
                            hmilyRepository.removeHmilyParticipantByDate(acquireDelayData(seconds));
                            hmilyRepository.removeHmilyParticipantUndoByDate(acquireDelayData(seconds));
                        } catch (Exception e) {
                            LOGGER.error(" scheduled hmily phyDeleted log is error:", e);
                        }
                    }, hmilyConfig.getScheduledInitDelay(), hmilyConfig.getScheduledPhyDeletedDelay(), TimeUnit.SECONDS);
        }
    }
    
    private void selfTccRecovery() {
        selfTccRecoveryExecutor
                .scheduleWithFixedDelay(() -> {
                    try {
                        List<HmilyParticipant> hmilyParticipantList =
                                hmilyRepository.listHmilyParticipant(acquireDelayData(hmilyConfig.getRecoverDelayTime()), TransTypeEnum.TCC.name(), hmilyConfig.getLimit());
                        if (CollectionUtils.isEmpty(hmilyParticipantList)) {
                            return;
                        }
                        for (HmilyParticipant hmilyParticipant : hmilyParticipantList) {
                            // if the try is not completed, no compensation will be provided (to prevent various exceptions in the try phase)
                            if (hmilyParticipant.getRetry() > hmilyConfig.getRetryMax()) {
                                LogUtil.error(LOGGER, "This hmily tcc transaction exceeds the maximum number of retries and no retries will occur：{}", () -> hmilyParticipant);
                                hmilyRepository.updateHmilyParticipantStatus(hmilyParticipant.getParticipantId(), HmilyActionEnum.DEATH.getCode());
                                continue;
                            }
                            if (hmilyParticipant.getStatus() == HmilyActionEnum.PRE_TRY.getCode()) {
                                //try not complete
                                continue;
                            }
                            final boolean successful = hmilyRepository.lockHmilyParticipant(hmilyParticipant);
                            // determine that rows > 0 is executed to prevent concurrency when the business side is in cluster mode
                            if (successful) {
                                LOGGER.info("hmily tcc transaction begin self recovery: {}", hmilyParticipant.toString());
                                HmilyTransaction globalHmilyTransaction = hmilyRepository.findByTransId(hmilyParticipant.getTransId());
                                if (Objects.isNull(globalHmilyTransaction)) {
                                    tccRecovery(hmilyParticipant.getStatus(), hmilyParticipant);
                                } else {
                                    tccRecovery(globalHmilyTransaction.getStatus(), hmilyParticipant);
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("hmily scheduled transaction log is error:", e);
                    }
                }, hmilyConfig.getScheduledInitDelay(), hmilyConfig.getScheduledRecoveryDelay(), TimeUnit.SECONDS);
    }
    
    private void tccRecovery(final int status, final HmilyParticipant hmilyParticipant) {
        if (status == HmilyActionEnum.TRYING.getCode() || status == HmilyActionEnum.CANCELING.getCode()) {
            hmilyTransactionRecoveryService.cancel(hmilyParticipant);
        } else if (status == HmilyActionEnum.CONFIRMING.getCode()) {
            hmilyTransactionRecoveryService.confirm(hmilyParticipant);
        }
    }
    
    private void selfTacRecovery() {
        selfTacRecoveryExecutor
                .scheduleWithFixedDelay(() -> {
                    try {
                        List<HmilyParticipant> hmilyParticipantList =
                                hmilyRepository.listHmilyParticipant(acquireDelayData(hmilyConfig.getRecoverDelayTime()), TransTypeEnum.TAC.name(), hmilyConfig.getLimit());
                        
                        if (CollectionUtils.isEmpty(hmilyParticipantList)) {
                            return;
                        }
                        for (HmilyParticipant hmilyParticipant : hmilyParticipantList) {
                            // if the try is not completed, no compensation will be provided (to prevent various exceptions in the try phase)
                            if (hmilyParticipant.getRetry() > hmilyConfig.getRetryMax()) {
                                LogUtil.error(LOGGER, "This tac transaction exceeds the maximum number of retries and no retries will occur：{}", () -> hmilyParticipant);
                                hmilyRepository.updateHmilyParticipantStatus(hmilyParticipant.getParticipantId(), HmilyActionEnum.DEATH.getCode());
                                continue;
                            }
                            final boolean successful = hmilyRepository.lockHmilyParticipant(hmilyParticipant);
                            // determine that rows > 0 is executed to prevent concurrency when the business side is in cluster mode
                            if (successful) {
                                List<HmilyParticipantUndo> participantUndoList = hmilyRepository.findHmilyParticipantUndoByParticipantId(hmilyParticipant.getParticipantId());
                                if (CollectionUtils.isEmpty(participantUndoList)) {
                                    continue;
                                }
                                HmilyTransaction globalHmilyTransaction = hmilyRepository.findByTransId(hmilyParticipant.getTransId());
                                if (Objects.isNull(globalHmilyTransaction)) {
                                    tacRecovery(hmilyParticipant.getStatus(), participantUndoList);
                                } else {
                                    tacRecovery(globalHmilyTransaction.getStatus(), participantUndoList);
                                }
                            }
                            hmilyRepository.removeHmilyParticipant(hmilyParticipant.getParticipantId());
                        }
                    } catch (Exception e) {
                        LOGGER.error("hmily scheduled transaction log is error:", e);
                    }
                }, hmilyConfig.getScheduledInitDelay(), hmilyConfig.getScheduledRecoveryDelay(), TimeUnit.SECONDS);
    }
    
    private void tacRecovery(final int status, final List<HmilyParticipantUndo> participantUndoList) {
        if (status == HmilyActionEnum.TRYING.getCode() || status == HmilyActionEnum.CANCELING.getCode()) {
            for (HmilyParticipantUndo undo : participantUndoList) {
                boolean success = UndoHook.INSTANCE.run(undo);
                if (success) {
                    hmilyRepository.removeHmilyParticipantUndo(undo.getUndoId());
                }
            }
        } else if (status == HmilyActionEnum.CONFIRMING.getCode()) {
            for (HmilyParticipantUndo undo : participantUndoList) {
                hmilyRepository.removeHmilyParticipantUndo(undo.getUndoId());
            }
        }
    }
    
    private void cleanHmilyTransaction() {
        cleanHmilyTransactionExecutor
                .scheduleWithFixedDelay(() -> {
                    try {
                        List<HmilyTransaction> hmilyTransactionList = hmilyRepository.listLimitByDelay(acquireDelayData(hmilyConfig.getCleanDelayTime()), hmilyConfig.getLimit());
                        if (CollectionUtils.isEmpty(hmilyTransactionList)) {
                            return;
                        }
                        for (HmilyTransaction hmilyTransaction : hmilyTransactionList) {
                            boolean exist = hmilyRepository.existHmilyParticipantByTransId(hmilyTransaction.getTransId());
                            if (!exist) {
                                hmilyRepository.removeHmilyTransaction(hmilyTransaction.getTransId());
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error(" scheduled clean hmily transaction log is error:", e);
                    }
                }, hmilyConfig.getScheduledInitDelay(), hmilyConfig.getScheduledCleanDelay(), TimeUnit.SECONDS);
    }
    
    private Date acquireDelayData(final int delayTime) {
        return new Date(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - (delayTime * 1000));
    }
    
    @Override
    public void close() {
        selfTccRecoveryExecutor.shutdown();
        selfTacRecoveryExecutor.shutdown();
        cleanHmilyTransactionExecutor.shutdown();
        if (Objects.nonNull(phyDeletedExecutor)) {
            phyDeletedExecutor.shutdown();
        }
    }
}
