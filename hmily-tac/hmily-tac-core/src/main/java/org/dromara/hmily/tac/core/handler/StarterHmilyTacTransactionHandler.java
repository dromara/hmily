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
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.disruptor.HmilyDisruptor;
import org.dromara.hmily.core.disruptor.handler.HmilyTransactionEventConsumer;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.core.service.HmilyTransactionTask;
import org.dromara.hmily.metrics.constant.LabelNames;
import org.dromara.hmily.metrics.reporter.MetricsReporter;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.tac.core.transaction.HmilyTacTransactionManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


/**
 * this is hmily transaction starter.
 *
 * @author xiaoyu
 */
public class StarterHmilyTacTransactionHandler implements HmilyTransactionHandler, AutoCloseable {
    
    private final HmilyTacTransactionManager tm = HmilyTacTransactionManager.getInstance();
    
    private final HmilyDisruptor<HmilyTransactionTask> disruptor;
    
    static {
        MetricsReporter.registerCounter(LabelNames.TRANSACTION_TOTAL, new String[]{"type"}, "hmily transaction total count");
        MetricsReporter.registerHistogram(LabelNames.TRANSACTION_LATENCY, "hmily transaction Latency Histogram Millis (ms)");
    }
    
    
    /**
     * Instantiates a new Starter hmily tac transaction handler.
     */
    public StarterHmilyTacTransactionHandler() {
        disruptor = new HmilyDisruptor<>(new HmilyTransactionEventConsumer(),
                Runtime.getRuntime().availableProcessors() << 1, HmilyDisruptor.DEFAULT_SIZE);
        disruptor.startup();
    }
    
    @Override
    public Object handleTransaction(final ProceedingJoinPoint point, final HmilyTransactionContext context)
            throws Throwable {
        Object returnValue;
        MetricsReporter.counterIncrement(LabelNames.TRANSACTION_TOTAL, new String[]{TransTypeEnum.TAC.name()});
        LocalDateTime starterTime = LocalDateTime.now();
        try {
            tm.begin(point);
            try {
                //execute try
                returnValue = point.proceed();
            } catch (Throwable throwable) {
                //if exception ,execute cancel
                final HmilyTransaction currentTransaction = tm.getHmilyTransaction();
                disruptor.getProvider().onData(() -> {
                    MetricsReporter.counterIncrement(LabelNames.TRANSACTION_STATUS, new String[]{TransTypeEnum.TAC.name(), HmilyRoleEnum.START.name(), HmilyActionEnum.CANCELING.name()});
                    tm.rollback(currentTransaction);
                });
                throw throwable;
            }
            // execute confirm
            final HmilyTransaction currentTransaction = tm.getHmilyTransaction();
            disruptor.getProvider().onData(() -> {
                MetricsReporter.counterIncrement(LabelNames.TRANSACTION_STATUS, new String[]{TransTypeEnum.TAC.name(), HmilyRoleEnum.START.name(), HmilyActionEnum.CONFIRMING.name()});
                tm.commit(currentTransaction);
            });
        } finally {
            HmilyContextHolder.remove();
            tm.remove();
            MetricsReporter.recordTime(LabelNames.TRANSACTION_LATENCY, starterTime.until(LocalDateTime.now(), ChronoUnit.MILLIS));
        }
        return returnValue;
    }
    
    @Override
    public void close() {
        disruptor.getProvider().shutdown();
    }
}
