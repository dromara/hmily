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

package com.hmily.tcc.core.service.handler;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hmily.tcc.common.bean.context.TccTransactionContext;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.enums.TccActionEnum;
import com.hmily.tcc.common.utils.DefaultValueUtils;
import com.hmily.tcc.core.cache.TccTransactionCacheManager;
import com.hmily.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.hmily.tcc.core.service.HmilyTransactionHandler;
import com.hmily.tcc.core.service.executor.HmilyTransactionExecutor;

/**
 * Participant Handler.
 *
 * @author xiaoyu
 */
@Component
public class ParticipantHmilyTransactionHandler implements HmilyTransactionHandler {

    private final HmilyTransactionExecutor hmilyTransactionExecutor;

    @Autowired
    public ParticipantHmilyTransactionHandler(final HmilyTransactionExecutor hmilyTransactionExecutor) {
        this.hmilyTransactionExecutor = hmilyTransactionExecutor;
    }

    @Override
    public Object handler(final ProceedingJoinPoint point, final TccTransactionContext context) throws Throwable {
        TccTransaction tccTransaction = null;
        TccTransaction currentTransaction;
        switch (TccActionEnum.acquireByCode(context.getAction())) {
            case TRYING:
                try {
                    tccTransaction = hmilyTransactionExecutor.beginParticipant(context, point);
                    final Object proceed = point.proceed();
                    tccTransaction.setStatus(TccActionEnum.TRYING.getCode());
                    //update log status to try
                    hmilyTransactionExecutor.updateStatus(tccTransaction);
                    return proceed;
                } catch (Throwable throwable) {
                    //if exception ,delete log.
                    hmilyTransactionExecutor.deleteTransaction(tccTransaction);
                    throw throwable;
                } finally {
                	TransactionContextLocal.getInstance().remove();
                }
            case CONFIRMING:
                currentTransaction = TccTransactionCacheManager.getInstance().getTccTransaction(context.getTransId());
                hmilyTransactionExecutor.confirm(currentTransaction);
                break;
            case CANCELING:
                currentTransaction = TccTransactionCacheManager.getInstance().getTccTransaction(context.getTransId());
                hmilyTransactionExecutor.cancel(currentTransaction);
                break;
            default:
                break;
        }
        Method method = ((MethodSignature) (point.getSignature())).getMethod();
        return DefaultValueUtils.getDefaultValue(method.getReturnType());
    }

}
