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

package org.dromara.hmily.core.service.recovery;

import com.google.common.collect.Lists;
import org.dromara.hmily.common.bean.entity.HmilyParticipant;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.dromara.hmily.core.reflect.HmilyReflector;
import org.dromara.hmily.core.spi.HmilyCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The type Hmily transaction recovery service.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyTransactionRecoveryService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyTransactionRecoveryService.class);

    private HmilyCoordinatorRepository hmilyCoordinatorRepository;

    /**
     * Instantiates a new Hmily transaction recovery service.
     *
     * @param hmilyCoordinatorRepository the hmily coordinator repository
     */
    public HmilyTransactionRecoveryService(final HmilyCoordinatorRepository hmilyCoordinatorRepository) {
        this.hmilyCoordinatorRepository = hmilyCoordinatorRepository;
    }

    /**
     * Cancel.
     *
     * @param hmilyTransaction the hmily transaction
     */
    public void cancel(final HmilyTransaction hmilyTransaction) {
        final List<HmilyParticipant> hmilyParticipants = hmilyTransaction.getHmilyParticipants();
        List<HmilyParticipant> failList = Lists.newArrayListWithCapacity(hmilyParticipants.size());
        boolean success = true;
        if (CollectionUtils.isNotEmpty(hmilyParticipants)) {
            for (HmilyParticipant hmilyParticipant : hmilyParticipants) {
                try {
                    HmilyReflector.executor(hmilyParticipant.getTransId(),
                            HmilyActionEnum.CANCELING,
                            hmilyParticipant.getCancelHmilyInvocation());
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

    /**
     * Confirm.
     *
     * @param hmilyTransaction the hmily transaction
     */
    public void confirm(final HmilyTransaction hmilyTransaction) {
        final List<HmilyParticipant> hmilyParticipants = hmilyTransaction.getHmilyParticipants();
        List<HmilyParticipant> failList = Lists.newArrayListWithCapacity(hmilyParticipants.size());
        boolean success = true;
        if (CollectionUtils.isNotEmpty(hmilyParticipants)) {
            for (HmilyParticipant hmilyParticipant : hmilyParticipants) {
                try {
                    HmilyReflector.executor(hmilyParticipant.getTransId(),
                            HmilyActionEnum.CONFIRMING,
                            hmilyParticipant.getConfirmHmilyInvocation());
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
            deleteTransaction(currentTransaction.getTransId());
        } else {
            currentTransaction.setHmilyParticipants(failList);
            hmilyCoordinatorRepository.updateParticipant(currentTransaction);
        }
    }

    private void deleteTransaction(final String transId) {
        hmilyCoordinatorRepository.remove(transId);
    }
}
