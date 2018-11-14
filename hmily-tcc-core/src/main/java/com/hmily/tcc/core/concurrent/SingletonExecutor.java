package com.hmily.tcc.core.concurrent;


import com.hmily.tcc.core.concurrent.threadpool.HmilyThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

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

    private void onRejected() {
        LOGGER.info("...thread:{}, Saturation occurs, actuator:{}", Thread.currentThread().getName(), name);
    }

    public SingletonExecutor(String poolName) {
        super(1, 1, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(QUEUE_SIZE),
                HmilyThreadFactory.create(poolName, false),
                HANDLER);
        this.name = poolName;
    }

    public String getName() {
        return name;
    }
}