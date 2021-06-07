/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.tac.core.handler;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.utils.DefaultValueUtils;
import org.dromara.hmily.core.cache.HmilyParticipantCacheManager;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.repository.HmilyRepositoryStorage;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.metrics.constant.LabelNames;
import org.dromara.hmily.metrics.reporter.MetricsReporter;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.tac.core.transaction.HmilyTacParticipantCoordinator;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * Participant Handler.
 *
 * @author xiaoyu
 */
public class ParticipantHmilyTacTransactionHandler implements HmilyTransactionHandler {
    
    private final HmilyTacParticipantCoordinator coordinator = HmilyTacParticipantCoordinator.getInstance();
    
    static {
        MetricsReporter.registerCounter(LabelNames.TRANSACTION_STATUS, new String[]{"type", "role", "status"}, "collect hmily transaction count");
    }
    
    @Override
    public Object handleTransaction(final ProceedingJoinPoint point, final HmilyTransactionContext context) throws Throwable {
        HmilyParticipant hmilyParticipant = null;
        switch (HmilyActionEnum.acquireByCode(context.getAction())) {
            case TRYING:
                try {
                    hmilyParticipant = coordinator.beginParticipant(context, point);
                    final Object proceed = point.proceed();
                    hmilyParticipant.setStatus(HmilyActionEnum.TRYING.getCode());
                    //update log status to try
                    HmilyRepositoryStorage.updateHmilyParticipantStatus(hmilyParticipant);
                    return proceed;
                } catch (Throwable throwable) {
                    //if exception ,delete log.
                    if (Objects.nonNull(hmilyParticipant)) {
                        HmilyParticipantCacheManager.getInstance().removeByKey(hmilyParticipant.getParticipantId());
                    }
                    HmilyRepositoryStorage.removeHmilyParticipant(hmilyParticipant);
                    throw throwable;
                } finally {
                    HmilyContextHolder.remove();
                }
            case CONFIRMING:
                MetricsReporter.counterIncrement(LabelNames.TRANSACTION_STATUS, new String[]{TransTypeEnum.TAC.name(), HmilyRoleEnum.PARTICIPANT.name(), HmilyActionEnum.CONFIRMING.name()});
                List<HmilyParticipant> confirmList = HmilyParticipantCacheManager.getInstance().get(context.getParticipantId());
                coordinator.commitParticipant(confirmList, context.getParticipantId());
                break;
            case CANCELING:
                MetricsReporter.counterIncrement(LabelNames.TRANSACTION_STATUS, new String[]{TransTypeEnum.TAC.name(), HmilyRoleEnum.PARTICIPANT.name(), HmilyActionEnum.CANCELING.name()});
                List<HmilyParticipant> cancelList = HmilyParticipantCacheManager.getInstance().get(context.getParticipantId());
                coordinator.rollbackParticipant(cancelList, context.getParticipantId());
                break;
            default:
                break;
        }
        Method method = ((MethodSignature) (point.getSignature())).getMethod();
        return DefaultValueUtils.getDefaultValue(method.getReturnType());
    }
}
