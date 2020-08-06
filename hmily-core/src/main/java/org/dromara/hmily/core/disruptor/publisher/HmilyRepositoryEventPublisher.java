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

package org.dromara.hmily.core.disruptor.publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.dromara.hmily.common.enums.EventTypeEnum;
import org.dromara.hmily.config.HmilyConfig;
import org.dromara.hmily.core.concurrent.ConsistentHashSelector;
import org.dromara.hmily.core.concurrent.SingletonExecutor;
import org.dromara.hmily.core.disruptor.DisruptorProviderManage;
import org.dromara.hmily.core.disruptor.handler.HmilyRepositoryDataHandler;
import org.dromara.hmily.core.holder.SingletonHolder;
import org.dromara.hmily.core.repository.HmilyRepositoryDispatcher;
import org.dromara.hmily.core.repository.HmilyRepositoryEvent;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;

/**
 * event publisher.
 *
 * @author xiaoyu(Myth)
 */
public final class HmilyRepositoryEventPublisher implements AutoCloseable {
    
    private static final HmilyRepositoryEventPublisher INSTANCE = new HmilyRepositoryEventPublisher();
    
    private DisruptorProviderManage<HmilyRepositoryEvent> disruptorProviderManage;
    
    private final HmilyConfig hmilyConfig = SingletonHolder.INST.get(HmilyConfig.class);
    
    private HmilyRepositoryEventPublisher() {
        start();
    }
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static HmilyRepositoryEventPublisher getInstance() {
        return INSTANCE;
    }
    
    private void start() {
        List<SingletonExecutor> selects = new ArrayList<>();
        for (int i = 0; i < hmilyConfig.getConsumerThreads(); i++) {
            selects.add(new SingletonExecutor("hmily-log-disruptor" + i));
        }
        ConsistentHashSelector selector = new ConsistentHashSelector(selects);
        disruptorProviderManage =
                new DisruptorProviderManage<>(
                        new HmilyRepositoryDataHandler(selector), 1, hmilyConfig.getBufferSize());
        disruptorProviderManage.startup();
    }
    
    /**
     * publish disruptor event.
     *
     * @param hmilyTransaction {@linkplain HmilyTransaction }
     * @param type             {@linkplain EventTypeEnum}
     */
    public void publishEvent(final HmilyTransaction hmilyTransaction, final int type) {
        HmilyRepositoryEvent event = new HmilyRepositoryEvent();
        event.setType(type);
        event.setHmilyTransaction(hmilyTransaction);
        event.setTransId(hmilyTransaction.getTransId());
        push(event);
    }
    
    /**
     * Publish event.
     *
     * @param hmilyParticipantUndo the hmily participant undo
     * @param type                 the type
     */
    public void publishEvent(final HmilyParticipantUndo hmilyParticipantUndo, final int type) {
        HmilyRepositoryEvent event = new HmilyRepositoryEvent();
        event.setType(type);
        event.setTransId(hmilyParticipantUndo.getTransId());
        event.setHmilyParticipantUndo(hmilyParticipantUndo);
        disruptorProviderManage.getProvider().onData(event);
    }
    
    /**
     * Publish event.
     *
     * @param hmilyParticipant the hmily participant
     * @param type             the type
     */
    public void publishEvent(final HmilyParticipant hmilyParticipant, final int type) {
        HmilyRepositoryEvent event = new HmilyRepositoryEvent();
        event.setType(type);
        event.setTransId(hmilyParticipant.getTransId());
        event.setHmilyParticipant(hmilyParticipant);
        push(event);
    }
    
    private void push(final HmilyRepositoryEvent event) {
        if (Objects.nonNull(hmilyConfig) && hmilyConfig.isAsyncRepository()) {
            disruptorProviderManage.getProvider().onData(event);
        } else {
            HmilyRepositoryDispatcher.getInstance().doDispatcher(event);
        }
    }
    
    @Override
    public void close() {
        disruptorProviderManage.getProvider().shutdown();
    }
}
