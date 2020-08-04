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

package org.dromara.hmily.metrics.facade.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.common.concurrent.HmilyThreadFactory;

/**
 * Metrics thread pool executor.
 */
@Slf4j
public final class MetricsThreadPoolExecutor extends ThreadPoolExecutor {
    
    @Getter
    private final String name;
    
    /**
     * Instantiates a new Metrics thread pool executor.
     *
     * @param threadCount core and max thread count
     * @param queueSize   queue size
     */
    public MetricsThreadPoolExecutor(final int threadCount, final int queueSize) {
        super(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueSize),
                HmilyThreadFactory.create("metrics", true), buildRejectedExecutionHandler(queueSize));
        this.name = "metrics";
    }
    
    private static RejectedExecutionHandler buildRejectedExecutionHandler(final int size) {
        return (r, executor) -> {
            BlockingQueue<Runnable> queue = executor.getQueue();
            while (queue.size() >= size) {
                if (executor.isShutdown()) {
                    throw new RejectedExecutionException("metrics thread pool executor closed");
                }
                ((MetricsThreadPoolExecutor) executor).onRejected();
            }
            if (!executor.isShutdown()) {
                executor.execute(r);
            }
        };
    }
    
    private void onRejected() {
        log.info("...thread:{}, Saturation occurs, actuator:{}", Thread.currentThread().getName(), name);
    }
}

