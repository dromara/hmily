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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.utils.DefaultValueUtils;
import org.dromara.hmily.core.cache.HmilyTransactionGuavaCacheManager;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.core.service.executor.HmilyTransactionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Participant Handler.
 *
 * @author xiaoyu
 */
@Component
public class ParticipantHmilyTransactionHandler implements HmilyTransactionHandler {

    private final HmilyTransactionExecutor hmilyTransactionExecutor;

    /**
     * Instantiates a new Participant hmily transaction handler.
     *
     * @param hmilyTransactionExecutor the hmily transaction executor
     */
    @Autowired
    public ParticipantHmilyTransactionHandler(final HmilyTransactionExecutor hmilyTransactionExecutor) {
        this.hmilyTransactionExecutor = hmilyTransactionExecutor;
    }

    @Override
    public Object handler(final ProceedingJoinPoint point, final HmilyTransactionContext context) throws Throwable {
        HmilyTransaction hmilyTransaction = null;
        HmilyTransaction currentTransaction;
        switch (HmilyActionEnum.acquireByCode(context.getAction())) {
            case TRYING:
                try {
                    hmilyTransaction = hmilyTransactionExecutor.preTryParticipant(context, point);
                    final Object proceed = point.proceed();
                    hmilyTransaction.setStatus(HmilyActionEnum.TRYING.getCode());
                    //update log status to try
                    hmilyTransactionExecutor.updateStatus(hmilyTransaction);
                    return proceed;
                } catch (Throwable throwable) {
                    //if exception ,delete log.
                    hmilyTransactionExecutor.deleteTransaction(hmilyTransaction);
                    throw throwable;
                } finally {
                    HmilyTransactionContextLocal.getInstance().remove();
                }
            case CONFIRMING:
                currentTransaction = HmilyTransactionGuavaCacheManager
                        .getInstance().getHmilyTransaction(context.getTransId());
                return hmilyTransactionExecutor.confirm(currentTransaction);
            case CANCELING:
                currentTransaction = HmilyTransactionGuavaCacheManager
                        .getInstance().getHmilyTransaction(context.getTransId());
                return hmilyTransactionExecutor.cancel(currentTransaction);
            default:
                break;
        }
        Method method = ((MethodSignature) (point.getSignature())).getMethod();
        return DefaultValueUtils.getDefaultValue(method.getReturnType());
    }

}
