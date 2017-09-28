/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.happylifeplat.tcc.core.concurrent.threadpool;


import com.happylifeplat.tcc.common.config.TccConfig;
import com.happylifeplat.tcc.common.enums.BlockingQueueTypeEnum;
import com.happylifeplat.tcc.common.enums.RejectedPolicyTypeEnum;
import com.happylifeplat.tcc.core.concurrent.threadpool.policy.AbortPolicy;
import com.happylifeplat.tcc.core.concurrent.threadpool.policy.BlockingPolicy;
import com.happylifeplat.tcc.core.concurrent.threadpool.policy.CallerRunsPolicy;
import com.happylifeplat.tcc.core.concurrent.threadpool.policy.DiscardedPolicy;
import com.happylifeplat.tcc.core.concurrent.threadpool.policy.RejectedPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class TccTransactionThreadPool {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TccTransactionThreadPool.class);

    private static final String ThreadFactoryName = "tccTransaction";
    private static final int Max_Array_Queue = 1000;

    private TccConfig tccConfig;

    private ScheduledExecutorService scheduledExecutorService;

    private ExecutorService fixExecutorService;

    private static final ScheduledExecutorService singleThreadScheduledExecutor =
            Executors.newSingleThreadScheduledExecutor(TccTransactionThreadFactory.create(ThreadFactoryName, false));


    @PostConstruct
    public void init() {
        scheduledExecutorService = Executors.newScheduledThreadPool(tccConfig.getScheduledThreadMax(),
                TccTransactionThreadFactory.create(ThreadFactoryName, true));

        fixExecutorService = new ThreadPoolExecutor(tccConfig.getCoordinatorThreadMax(), tccConfig.getCoordinatorThreadMax(), 0, TimeUnit.MILLISECONDS,
                createBlockingQueue(),
                TccTransactionThreadFactory.create(ThreadFactoryName, false), createPolicy());

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
                return new ArrayBlockingQueue<>(Max_Array_Queue);
            case SYNCHRONOUS_QUEUE:
                return new SynchronousQueue<>();
            default:
                return new LinkedBlockingQueue<>();
        }

    }

    public ExecutorService newCustomFixedThreadPool(int threads) {
        return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS,
                createBlockingQueue(),
                TccTransactionThreadFactory.create(ThreadFactoryName, false), createPolicy());
    }

    public ExecutorService newFixedThreadPool() {
        return fixExecutorService;
    }

    public ExecutorService newSingleThreadExecutor() {
        return new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                createBlockingQueue(),
                TccTransactionThreadFactory.create(ThreadFactoryName, false), createPolicy());
    }

    public ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return singleThreadScheduledExecutor;
    }

    public ScheduledExecutorService newScheduledThreadPool() {
        return scheduledExecutorService;
    }


}

