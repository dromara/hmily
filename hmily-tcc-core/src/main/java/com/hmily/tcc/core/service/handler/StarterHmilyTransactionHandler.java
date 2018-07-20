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

import com.hmily.tcc.common.bean.context.TccTransactionContext;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.enums.TccActionEnum;
import com.hmily.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.hmily.tcc.core.service.HmilyTransactionHandler;
import com.hmily.tcc.core.service.executor.HmilyTransactionExecutor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * this is transaction starter.
 *
 * @author xiaoyu
 */
@Component
public class StarterHmilyTransactionHandler implements HmilyTransactionHandler {

    private final HmilyTransactionExecutor hmilyTransactionExecutor;

    @Autowired
    public StarterHmilyTransactionHandler(final HmilyTransactionExecutor hmilyTransactionExecutor) {
        this.hmilyTransactionExecutor = hmilyTransactionExecutor;
    }

    @Override
    public Object handler(final ProceedingJoinPoint point, TccTransactionContext context)
            throws Throwable {
        Object returnValue = null;
        try {
            TccTransaction tccTransaction;
            context = TransactionContextLocal.getInstance().get();
            if (context == null) {
                tccTransaction = hmilyTransactionExecutor.begin(point);
                try {
                    //execute try
                    returnValue = point.proceed();
                    tccTransaction.setStatus(TccActionEnum.TRYING.getCode());
                    hmilyTransactionExecutor.updateStatus(tccTransaction);
                } catch (Throwable throwable) {
                    //if exception ,execute cancel
                    hmilyTransactionExecutor
                            .cancel(hmilyTransactionExecutor.getCurrentTransaction());
                    throw throwable;
                }
                //execute confirm
                hmilyTransactionExecutor.confirm(hmilyTransactionExecutor.getCurrentTransaction());
            } else if (context.getAction() == TccActionEnum.CONFIRMING.getCode()) {
                //execute confirm
                hmilyTransactionExecutor.confirm(hmilyTransactionExecutor.getCurrentTransaction());
            }

        } finally {
            hmilyTransactionExecutor.remove();
        }
        return returnValue;
    }
}
