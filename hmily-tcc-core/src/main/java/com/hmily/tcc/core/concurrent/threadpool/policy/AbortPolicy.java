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
package com.hmily.tcc.core.concurrent.threadpool.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author xiaoyu(Myth)
 * @since JDK 1.8
 */
public class AbortPolicy extends ThreadPoolExecutor.AbortPolicy {
    private static final Logger LOG = LoggerFactory.getLogger(AbortPolicy.class);

    private String threadName;

    public AbortPolicy() {
        this(null);
    }

    public AbortPolicy(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        if (threadName != null) {
            LOG.error("tccTransaction Thread pool [{}] is exhausted, executor={}", threadName, executor.toString());
        }
        String msg = String.format("txTransaction["
                        + " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d),"
                        + " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)]",
                threadName, executor.getPoolSize(), executor.getActiveCount(), executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getLargestPoolSize(),
                executor.getTaskCount(), executor.getCompletedTaskCount(), executor.isShutdown(), executor.isTerminated(), executor.isTerminating());
        System.out.println(msg);
        super.rejectedExecution(runnable, executor);
    }
}

