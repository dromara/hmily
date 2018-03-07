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
import com.hmily.tcc.core.disruptor.event.TccTransactionEvent;
import com.hmily.tcc.core.disruptor.factory.TccTransactionEventFactory;
import com.hmily.tcc.core.disruptor.handler.TccTransactionEventHandler;
import com.hmily.tcc.core.disruptor.translator.TccTransactionEventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2018/3/5 15:01
 * @since JDK 1.8
 */
@Component
public class TccTransactionEventPublisher implements DisposableBean {

    private Disruptor<TccTransactionEvent> disruptor;

    @Autowired
    private TccTransactionEventHandler tccTransactionEventHandler;


    public void start(int bufferSize) {
        disruptor =
                new Disruptor<>(new TccTransactionEventFactory(),
                        bufferSize, r -> {
                    AtomicInteger index = new AtomicInteger(1);
                    return new Thread(null, r, "disruptor-thread-" + index.getAndIncrement());
                }, ProducerType.MULTI, new YieldingWaitStrategy());

        disruptor.handleEventsWith(tccTransactionEventHandler);
        disruptor.start();
    }

    public void publishEvent(TccTransaction tccTransaction,int type) {
        final RingBuffer<TccTransactionEvent> ringBuffer = disruptor.getRingBuffer();
        ringBuffer.publishEvent(new TccTransactionEventTranslator(type), tccTransaction);
    }



    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     *                   Exceptions will get logged but not rethrown to allow
     *                   other beans to release their resources too.
     */
    @Override
    public void destroy() throws Exception {
        disruptor.shutdown();
    }
}
