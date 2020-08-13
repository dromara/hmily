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

import java.util.Optional;
import java.util.function.Supplier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.disruptor.DisruptorProviderManage;
import org.dromara.hmily.core.disruptor.handler.HmilyTransactionExecutorHandler;
import org.dromara.hmily.core.holder.HmilyTransactionHolder;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.core.service.HmilyTransactionHandlerAlbum;
import org.dromara.hmily.metrics.enums.MetricsLabelEnum;
import org.dromara.hmily.metrics.spi.MetricsHandlerFacade;
import org.dromara.hmily.metrics.spi.MetricsHandlerFacadeEngine;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.tcc.executor.HmilyTccTransactionExecutor;


/**
 * this is hmily transaction starter.
 *
 * @author xiaoyu
 */
public class StarterHmilyTccTransactionHandler implements HmilyTransactionHandler, AutoCloseable {
    
    private final HmilyTccTransactionExecutor executor = HmilyTccTransactionExecutor.getInstance();
    
    private DisruptorProviderManage<HmilyTransactionHandlerAlbum> disruptorProviderManage;
    
    public StarterHmilyTccTransactionHandler() {
        disruptorProviderManage = new DisruptorProviderManage<>(new HmilyTransactionExecutorHandler(),
                Runtime.getRuntime().availableProcessors() << 1, DisruptorProviderManage.DEFAULT_SIZE);
        disruptorProviderManage.startup();
    }
    
    @Override
    public Object handler(final ProceedingJoinPoint point, final HmilyTransactionContext context)
            throws Throwable {
        Object returnValue;
        Supplier<Boolean> histogramSupplier = null;
        Optional<MetricsHandlerFacade> handlerFacade = MetricsHandlerFacadeEngine.load();
        try {
            if (handlerFacade.isPresent()) {
                handlerFacade.get().counterIncrement(MetricsLabelEnum.TRANSACTION_TOTAL.getName(), TransTypeEnum.TCC.name());
                histogramSupplier = handlerFacade.get().histogramStartTimer(MetricsLabelEnum.TRANSACTION_LATENCY.getName(), TransTypeEnum.TCC.name());
            }
            HmilyTransaction hmilyTransaction = executor.preTry(point);
            try {
                //execute try
                returnValue = point.proceed();
                hmilyTransaction.setStatus(HmilyActionEnum.TRYING.getCode());
                executor.updateStartStatus(hmilyTransaction);
            } catch (Throwable throwable) {
                //if exception ,execute cancel
                final HmilyTransaction currentTransaction = HmilyTransactionHolder.getInstance().getCurrentTransaction();
                disruptorProviderManage.getProvider().onData(() -> {
                    handlerFacade.ifPresent(metricsHandlerFacade -> metricsHandlerFacade.counterIncrement(MetricsLabelEnum.TRANSACTION_STATUS.getName(),
                            TransTypeEnum.TCC.name(), HmilyRoleEnum.START.name(), HmilyActionEnum.CANCELING.name()));
                    executor.globalCancel(currentTransaction);
                });
                throw throwable;
            }
            //execute confirm
            final HmilyTransaction currentTransaction = HmilyTransactionHolder.getInstance().getCurrentTransaction();
            disruptorProviderManage.getProvider().onData(() -> {
                handlerFacade.ifPresent(metricsHandlerFacade -> metricsHandlerFacade.counterIncrement(MetricsLabelEnum.TRANSACTION_STATUS.getName(),
                        TransTypeEnum.TCC.name(), HmilyRoleEnum.START.name(), HmilyActionEnum.CONFIRMING.name()));
                executor.globalConfirm(currentTransaction);
            });
        } finally {
            HmilyContextHolder.remove();
            executor.remove();
            if (null != histogramSupplier) {
                histogramSupplier.get();
            }
        }
        return returnValue;
    }
    
    @Override
    public void close() {
        disruptorProviderManage.getProvider().shutdown();
    }
}
