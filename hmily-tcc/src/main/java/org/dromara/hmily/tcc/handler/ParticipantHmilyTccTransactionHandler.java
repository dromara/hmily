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

package org.dromara.hmily.tcc.handler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import org.dromara.hmily.metrics.enums.MetricsLabelEnum;
import org.dromara.hmily.metrics.spi.MetricsHandlerFacade;
import org.dromara.hmily.metrics.spi.MetricsHandlerFacadeEngine;
import org.dromara.hmily.tcc.executor.HmilyTccTransactionExecutor;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;

/**
 * Participant Handler.
 *
 * @author xiaoyu
 */
public class ParticipantHmilyTccTransactionHandler implements HmilyTransactionHandler {
    
    private final HmilyTccTransactionExecutor executor = HmilyTccTransactionExecutor.getInstance();
    
    @Override
    public Object handler(final ProceedingJoinPoint point, final HmilyTransactionContext context) throws Throwable {
        HmilyParticipant hmilyParticipant = null;
        switch (HmilyActionEnum.acquireByCode(context.getAction())) {
            case TRYING:
                try {
                    hmilyParticipant = executor.preTryParticipant(context, point);
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
                MetricsHandlerFacadeEngine.load().ifPresent(metricsHandlerFacade -> metricsHandlerFacade.counterIncrement(MetricsLabelEnum.TRANSACTION_STATUS.getName(),
                        TransTypeEnum.TCC.name(), HmilyRoleEnum.PARTICIPANT.name(), HmilyActionEnum.CONFIRMING.name()));
                List<HmilyParticipant> confirmList = HmilyParticipantCacheManager.getInstance().get(context.getParticipantId());
                return executor.participantConfirm(confirmList, context.getParticipantId());
            case CANCELING:
                MetricsHandlerFacadeEngine.load().ifPresent(metricsHandlerFacade -> metricsHandlerFacade.counterIncrement(MetricsLabelEnum.TRANSACTION_STATUS.getName(),
                        TransTypeEnum.TCC.name(), HmilyRoleEnum.PARTICIPANT.name(), HmilyActionEnum.CANCELING.name()));
                List<HmilyParticipant> cancelList = HmilyParticipantCacheManager.getInstance().get(context.getParticipantId());
                return executor.participantCancel(cancelList, context.getParticipantId());
            default:
                break;
        }
        Method method = ((MethodSignature) (point.getSignature())).getMethod();
        return DefaultValueUtils.getDefaultValue(method.getReturnType());
    }
}
