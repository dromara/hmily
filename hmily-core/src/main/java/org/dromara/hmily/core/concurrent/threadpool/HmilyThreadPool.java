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

package org.dromara.hmily.core.concurrent.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * HmilyThreadPool.
 * Customize thread Pool .
 *
 * @author chenbin sixh
 */
public final class HmilyThreadPool extends DelegationThreadPoolExecutor {

    /**
     * Initialize the multi-end thread pool.
     *
     * @param coreSize the core size
     * @param maxSize  the max size
     * @param poolName the pool name
     */
    public HmilyThreadPool(final int coreSize, final int maxSize, final String poolName) {
        this(coreSize, maxSize, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                HmilyThreadFactory.create(poolName, false));
    }

    /**
     * Initialize a thread pool.
     *
     * @param poolName name;
     */
    public HmilyThreadPool(final String poolName) {
        this(1, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), HmilyThreadFactory.create(poolName, false));
    }

    /**
     * Initialize a thread pool with the same pool size as the maximum pool size.
     *
     * @param corePoolSize  corePoolSize
     * @param keepAliveTime keepAliveTime
     * @param unit          unit
     * @param workQueue     workQueue
     * @param threadFactory threadFactory
     * @see java.util.concurrent.ThreadPoolExecutor
     */
    public HmilyThreadPool(final int corePoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory) {
        this(corePoolSize, corePoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * Initialize a thread pool.
     *
     * @param corePoolSize    corePoolSize
     * @param maximumPoolSize maximumPoolSize
     * @param keepAliveTime   keepAliveTime
     * @param unit            unit
     * @param workQueue       workQueue
     * @param threadFactory   workQueue
     * @see java.util.concurrent.ThreadPoolExecutor
     */
    public HmilyThreadPool(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime,
                           final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, HANDLER);
    }
}
