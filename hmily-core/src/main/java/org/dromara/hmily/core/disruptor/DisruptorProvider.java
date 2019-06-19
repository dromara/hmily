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

package org.dromara.hmily.core.disruptor;

import com.lmax.disruptor.RingBuffer;
import org.dromara.hmily.core.disruptor.event.DataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DisruptorProvider.
 * disruptor provider definition.
 *
 * @param <T> the type parameter
 * @author chenbin sixh
 */
public class DisruptorProvider<T> {

    private final RingBuffer<DataEvent<T>> ringBuffer;

    /**
     * The Logger.
     */
    private Logger logger = LoggerFactory.getLogger(DisruptorProvider.class);

    /**
     * Instantiates a new Disruptor provider.
     *
     * @param ringBuffer the ring buffer
     */
    DisruptorProvider(final RingBuffer<DataEvent<T>> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * push data to disruptor queue.
     *
     * @param t the t
     */
    public void onData(final T t) {
        long position = ringBuffer.next();
        try {
            DataEvent<T> de = ringBuffer.get(position);
            de.setT(t);
            ringBuffer.publish(position);
        } catch (Exception ex) {
            logger.error("push data error:", ex);
        }
    }
}
