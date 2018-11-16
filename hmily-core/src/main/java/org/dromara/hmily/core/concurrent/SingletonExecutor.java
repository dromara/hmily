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

package org.dromara.hmily.core.concurrent;

import org.dromara.hmily.core.concurrent.threadpool.HmilyThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * The execution thread of a single task.
 *
 * @author chenbin
 */
public class SingletonExecutor extends ThreadPoolExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingletonExecutor.class);

    private static final int QUEUE_SIZE = 5000;

    private static final RejectedExecutionHandler HANDLER = (r, executor) -> {
        BlockingQueue<Runnable> queue = executor.getQueue();
        while (queue.size() >= QUEUE_SIZE) {
            if (executor.isShutdown()) {
                throw new RejectedExecutionException("SingletonExecutor closed");
            }
            try {
                ((SingletonExecutor) executor).onRejected();
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
        executor.execute(r);
    };

    /**
     * thread name.
     */
    private String name;

    /**
     * Instantiates a new Singleton executor.
     *
     * @param poolName the pool name
     */
    public SingletonExecutor(final String poolName) {
        super(1, 1, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(QUEUE_SIZE),
                HmilyThreadFactory.create(poolName, false),
                HANDLER);
        this.name = poolName;
    }

    private void onRejected() {
        LOGGER.info("...thread:{}, Saturation occurs, actuator:{}", Thread.currentThread().getName(), name);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}
