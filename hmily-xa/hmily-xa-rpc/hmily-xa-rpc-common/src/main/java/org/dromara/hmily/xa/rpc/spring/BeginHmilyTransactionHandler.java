/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dromara.hmily.xa.rpc.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.metrics.constant.LabelNames;
import org.dromara.hmily.metrics.reporter.MetricsReporter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * CommitHmilyTransactionHandler .
 * 提交一个事务的相关处理.
 *
 * @author sixh chenbin
 */
public class BeginHmilyTransactionHandler implements HmilyTransactionHandler {
    
    static {
        MetricsReporter.registerCounter(LabelNames.TRANSACTION_TOTAL, new String[]{"type"}, "hmily transaction total count");
        MetricsReporter.registerHistogram(LabelNames.TRANSACTION_LATENCY, new String[]{"type"}, "hmily transaction Latency Histogram Millis (ms)");
        MetricsReporter.registerCounter(LabelNames.TRANSACTION_STATUS, new String[]{"type", "role", "status"}, "collect hmily transaction count");
    }

    @Override
    public Object handleTransaction(final ProceedingJoinPoint point, final HmilyTransactionContext hmilyTransactionContext) throws Throwable {
        HmilyContextHolder.set(hmilyTransactionContext);
        MetricsReporter.counterIncrement(LabelNames.TRANSACTION_TOTAL, new String[]{TransTypeEnum.XA.name()});
        LocalDateTime starterTime = LocalDateTime.now();
        try {
            return point.proceed();  
        } finally {
            MetricsReporter.recordTime(LabelNames.TRANSACTION_LATENCY, new String[]{TransTypeEnum.XA.name()}, starterTime.until(LocalDateTime.now(), ChronoUnit.MILLIS));
        }
    }
}
