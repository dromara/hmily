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
 *//*

package com.hmily.tcc.core.concurrent.threadpool;


import com.hmily.tcc.common.config.TccConfig;
import com.hmily.tcc.common.enums.BlockingQueueTypeEnum;
import com.hmily.tcc.common.enums.RejectedPolicyTypeEnum;
import com.hmily.tcc.core.concurrent.threadpool.policy.AbortPolicy;
import com.hmily.tcc.core.concurrent.threadpool.policy.BlockingPolicy;
import com.hmily.tcc.core.concurrent.threadpool.policy.CallerRunsPolicy;
import com.hmily.tcc.core.concurrent.threadpool.policy.DiscardedPolicy;
import com.hmily.tcc.core.concurrent.threadpool.policy.RejectedPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

*/
/**
 * @author xiaoyu
 *//*

public class TccTransactionThreadPool {

    */
/**
     * logger
     *//*

    private static final Logger LOGGER = LoggerFactory.getLogger(TccTransactionThreadPool.class);

    private static final String THREAD_FACTORY_NAME = "tccTransaction";
    private static final int MAX_ARRAY_QUEUE = 1000;

    private TccConfig tccConfig;

    private ScheduledExecutorService scheduledExecutorService;

    private ExecutorService fixExecutorService;

    private static final ScheduledExecutorService SCHEDULED_THREAD_POOL_EXECUTOR =
            new ScheduledThreadPoolExecutor(1,
                    TccTransactionThreadFactory.create(THREAD_FACTORY_NAME, true));


    @PostConstruct
    public void init() {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1,
                TccTransactionThreadFactory.create(THREAD_FACTORY_NAME, true));

        fixExecutorService = new ThreadPoolExecutor(tccConfig.getCoordinatorThreadMax(), tccConfig.getCoordinatorThreadMax(), 0, TimeUnit.MILLISECONDS,
                createBlockingQueue(),
                TccTransactionThreadFactory.create(THREAD_FACTORY_NAME, false), createPolicy());

    }


    @Autowired
    public TccTransactionThreadPool(TccConfig tccConfig) {
        this.tccConfig = tccConfig;
    }


    private RejectedExecutionHandler createPolicy() {
        RejectedPolicyTypeEnum rejectedPolicyType = RejectedPolicyTypeEnum.fromString(tccConfig.getRejectPolicy());
        switch (rejectedPolicyType) {
            case BLOCKING_POLICY:
                return new BlockingPolicy();
            case CALLER_RUNS_POLICY:
                return new CallerRunsPolicy();
            case ABORT_POLICY:
                return new AbortPolicy();
            case REJECTED_POLICY:
                return new RejectedPolicy();
            case DISCARDED_POLICY:
                return new DiscardedPolicy();
            default:
                return new AbortPolicy();
        }
    }

    private BlockingQueue<Runnable> createBlockingQueue() {
        BlockingQueueTypeEnum queueType = BlockingQueueTypeEnum.fromString(tccConfig.getBlockingQueueType());

        switch (queueType) {
            case LINKED_BLOCKING_QUEUE:
                return new LinkedBlockingQueue<>();
            case ARRAY_BLOCKING_QUEUE:
                return new ArrayBlockingQueue<>(MAX_ARRAY_QUEUE);
            case SYNCHRONOUS_QUEUE:
                return new SynchronousQueue<>();
            default:
                return new LinkedBlockingQueue<>();
        }

    }

    public ExecutorService newCustomFixedThreadPool(int threads) {
        return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS,
                createBlockingQueue(),
                TccTransactionThreadFactory.create(THREAD_FACTORY_NAME, false), createPolicy());
    }

    public ExecutorService newFixedThreadPool() {
        return fixExecutorService;
    }

    public ExecutorService newSingleThreadExecutor() {
        return new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                createBlockingQueue(),
                TccTransactionThreadFactory.create(THREAD_FACTORY_NAME, false), createPolicy());
    }

    public ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return SCHEDULED_THREAD_POOL_EXECUTOR;
    }

    public ScheduledExecutorService newScheduledThreadPool() {
        return scheduledExecutorService;
    }


}

*/
