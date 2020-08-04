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

package org.dromara.hmily.tac.core.handler;

import org.aspectj.lang.ProceedingJoinPoint;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.disruptor.DisruptorProviderManage;
import org.dromara.hmily.core.disruptor.handler.HmilyTransactionExecutorHandler;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.core.service.HmilyTransactionHandlerAlbum;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.tac.core.transaction.HmilyTacGlobalTransaction;


/**
 * this is hmily transaction starter.
 *
 * @author xiaoyu
 */
public class StarterHmilyTacTransactionHandler implements HmilyTransactionHandler, AutoCloseable {
    
    private final HmilyTacGlobalTransaction globalTransaction = HmilyTacGlobalTransaction.getInstance();
    
    private final DisruptorProviderManage<HmilyTransactionHandlerAlbum> disruptorProviderManage;
    
    public StarterHmilyTacTransactionHandler() {
        disruptorProviderManage = new DisruptorProviderManage<>(new HmilyTransactionExecutorHandler(),
                Runtime.getRuntime().availableProcessors() << 1, DisruptorProviderManage.DEFAULT_SIZE);
        disruptorProviderManage.startup();
    }
    
    @Override
    public Object handler(final ProceedingJoinPoint point, final HmilyTransactionContext context)
            throws Throwable {
        Object returnValue;
        try {
            HmilyTransaction hmilyTransaction = globalTransaction.begin();
            try {
                //execute try
                returnValue = point.proceed();
            } catch (Throwable throwable) {
                //if exception ,execute cancel
                final HmilyTransaction currentTransaction = globalTransaction.getHmilyTransaction();
                disruptorProviderManage.getProvider().onData(() -> globalTransaction.rollback(currentTransaction));
                throw throwable;
            }
            // execute confirm
            final HmilyTransaction currentTransaction = globalTransaction.getHmilyTransaction();
            disruptorProviderManage.getProvider().onData(() -> globalTransaction.commit(currentTransaction));
        } finally {
            HmilyContextHolder.remove();
            globalTransaction.remove();
        }
        return returnValue;
    }
    
    @Override
    public void close() {
        disruptorProviderManage.getProvider().shutdown();
    }
}
