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

import org.dromara.hmily.common.enums.ExecutorTypeEnum;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.core.reflect.HmilyReflector;
import org.dromara.hmily.core.repository.HmilyRepositoryFacade;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Hmily transaction recovery service.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyTransactionRecoveryService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyTransactionRecoveryService.class);
    
    /**
     * Cancel.
     *
     * @param hmilyParticipant the hmily participant
     * @return the boolean
     */
    public boolean cancel(final HmilyParticipant hmilyParticipant) {
        try {
            HmilyReflector.executor(HmilyActionEnum.CANCELING, ExecutorTypeEnum.LOCAL, hmilyParticipant);
            return removeHmilyParticipant(hmilyParticipant.getParticipantId());
        } catch (Exception e) {
            LOGGER.error("hmily Recovery executor cancel exception:", e);
            return false;
        }
    }
    
    /**
     * Confirm.
     *
     * @param hmilyParticipant the hmily participant
     * @return the boolean
     */
    public boolean confirm(final HmilyParticipant hmilyParticipant) {
        try {
            HmilyReflector.executor(HmilyActionEnum.CONFIRMING, ExecutorTypeEnum.LOCAL, hmilyParticipant);
            return removeHmilyParticipant(hmilyParticipant.getParticipantId());
        } catch (Exception e) {
            LOGGER.error("hmily Recovery executor confirm exception:", e);
            return false;
        }
    }
    
    private boolean removeHmilyParticipant(final Long participantId) {
        return HmilyRepositoryFacade.getInstance().removeHmilyParticipant(participantId);
    }
}
