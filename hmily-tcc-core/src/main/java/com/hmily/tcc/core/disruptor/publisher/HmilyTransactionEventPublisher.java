/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.hmily.tcc.core.disruptor.publisher;

import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.enums.EventTypeEnum;
import com.hmily.tcc.core.concurrent.threadpool.HmilyThreadFactory;
import com.hmily.tcc.core.coordinator.CoordinatorService;
import com.hmily.tcc.core.disruptor.event.HmilyTransactionEvent;
import com.hmily.tcc.core.disruptor.factory.HmilyTransactionEventFactory;
import com.hmily.tcc.core.disruptor.handler.HmilyConsumerDataHandler;
import com.hmily.tcc.core.disruptor.translator.HmilyTransactionEventTranslator;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * event publisher.
 *
 * @author xiaoyu(Myth)
 */
@Component
public class HmilyTransactionEventPublisher implements DisposableBean {

    private Disruptor<HmilyTransactionEvent> disruptor;

    private final CoordinatorService coordinatorService;

    @Autowired
    public HmilyTransactionEventPublisher(final CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    /**
     * disruptor start.
     *
     * @param bufferSize this is disruptor buffer size.
     * @param threadSize this is disruptor consumer thread size.
     */
    public void start(final int bufferSize, final int threadSize) {
        disruptor = new Disruptor<>(new HmilyTransactionEventFactory(), bufferSize, r -> {
            AtomicInteger index = new AtomicInteger(1);
            return new Thread(null, r, "disruptor-thread-" + index.getAndIncrement());
        }, ProducerType.MULTI, new BlockingWaitStrategy());

        final Executor executor = new ThreadPoolExecutor(threadSize, threadSize, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                HmilyThreadFactory.create("hmily-log-disruptor", false),
                new ThreadPoolExecutor.AbortPolicy());

        HmilyConsumerDataHandler[] consumers = new HmilyConsumerDataHandler[threadSize];
        for (int i = 0; i < threadSize; i++) {
            consumers[i] = new HmilyConsumerDataHandler(executor, coordinatorService);
        }
        disruptor.handleEventsWithWorkerPool(consumers);
        disruptor.setDefaultExceptionHandler(new IgnoreExceptionHandler());
        disruptor.start();
    }

    /**
     * publish disruptor event.
     *
     * @param tccTransaction {@linkplain com.hmily.tcc.common.bean.entity.TccTransaction }
     * @param type           {@linkplain EventTypeEnum}
     */
    public void publishEvent(final TccTransaction tccTransaction, final int type) {
        final RingBuffer<HmilyTransactionEvent> ringBuffer = disruptor.getRingBuffer();
        ringBuffer.publishEvent(new HmilyTransactionEventTranslator(type), tccTransaction);
    }

    @Override
    public void destroy() {
        disruptor.shutdown();
    }

}
