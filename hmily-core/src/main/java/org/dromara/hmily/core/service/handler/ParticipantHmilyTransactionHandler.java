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

package org.dromara.hmily.core.service.handler;

import java.lang.reflect.Method;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.utils.DefaultValueUtils;
import org.dromara.hmily.core.cache.HmilyParticipantCacheManager;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.core.service.executor.HmilyTransactionExecutor;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;

/**
 * Participant Handler.
 *
 * @author xiaoyu
 */
public class ParticipantHmilyTransactionHandler implements HmilyTransactionHandler {
    
    private final HmilyTransactionExecutor executor = HmilyTransactionExecutor.getInstance();
    
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
                    executor.updateHmilyParticipantStatus(hmilyParticipant);
                    return proceed;
                } catch (Throwable throwable) {
                    //if exception ,delete log.
                    executor.removeHmilyParticipant(hmilyParticipant);
                    throw throwable;
                } finally {
                    HmilyContextHolder.remove();
                }
            case CONFIRMING:
                List<HmilyParticipant> confirmList = HmilyParticipantCacheManager.getInstance().get(context.getParticipantId());
                return executor.participantConfirm(confirmList);
            case CANCELING:
                List<HmilyParticipant> cancelList = HmilyParticipantCacheManager.getInstance().get(context.getParticipantId());
                return executor.participantCancel(cancelList);
            default:
                break;
        }
        Method method = ((MethodSignature) (point.getSignature())).getMethod();
        return DefaultValueUtils.getDefaultValue(method.getReturnType());
    }

}
