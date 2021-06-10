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

package org.dromara.hmily.core.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.dromara.hmily.common.concurrent.HmilyThreadFactory;
import org.dromara.hmily.core.disruptor.event.DataEvent;

/**
 * Hmily disruptor.
 * disruptor provider manager.
 *
 * @param <T> the type parameter
 * @author xiaoyu sixh
 */
public class HmilyDisruptor<T> {

    public static final Integer DEFAULT_SIZE = 4096 << 1 << 1;

    private static final Integer DEFAULT_CONSUMER_SIZE = Runtime.getRuntime().availableProcessors() << 1;

    private final Integer size;

    private DisruptorProvider<T> provider;

    private Integer consumerSize;

    private HmilyDisruptorConsumer<T> consumer;

    /**
     * Instantiates a new Disruptor provider manage.
     *
     * @param consumer the consumer factory
     * @param ringBufferSize  the size
     */
    public HmilyDisruptor(final HmilyDisruptorConsumer<T> consumer, final Integer ringBufferSize) {
        this(consumer,
                DEFAULT_CONSUMER_SIZE,
                ringBufferSize);
    }

    /**
     * Instantiates a new Disruptor provider manage.
     *
     * @param consumer the consumer factory
     */
    public HmilyDisruptor(final HmilyDisruptorConsumer<T> consumer) {
        this(consumer, DEFAULT_CONSUMER_SIZE, DEFAULT_SIZE);
    }

    /**
     * Instantiates a new Disruptor provider manage.
     *
     * @param consumer the consumer factory
     * @param consumerSize    the consumer size
     * @param ringBufferSize  the ringBuffer size
     */
    public HmilyDisruptor(final HmilyDisruptorConsumer<T> consumer,
                          final int consumerSize,
                          final int ringBufferSize) {
        this.consumer = consumer;
        this.size = ringBufferSize;
        this.consumerSize = consumerSize;
    }

    /**
     * start disruptor.
     */
    @SuppressWarnings("unchecked")
    public void startup() {
        Disruptor<DataEvent<T>> disruptor = new Disruptor<>(new DisruptorEventFactory<>(),
                size,
                HmilyThreadFactory.create("disruptor_consumer_" + consumer.fixName(), false),
                ProducerType.MULTI,
                new BlockingWaitStrategy());
        HmilyDisruptorWorkHandler<T>[] workerPool = new HmilyDisruptorWorkHandler[consumerSize];
        for (int i = 0; i < consumerSize; i++) {
            workerPool[i] = new HmilyDisruptorWorkHandler<>(consumer);
        }
        disruptor.handleEventsWithWorkerPool(workerPool);
        disruptor.setDefaultExceptionHandler(new IgnoreExceptionHandler());
        disruptor.start();
        RingBuffer<DataEvent<T>> ringBuffer = disruptor.getRingBuffer();
        provider = new DisruptorProvider<>(ringBuffer, disruptor);
    }
    
    /**
     * Gets provider.
     *
     * @return the provider
     */
    public DisruptorProvider<T> getProvider() {
        return provider;
    }
}
