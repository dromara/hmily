package org.dromara.hmily.core.concurrent.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * DelegationThreadPoolExecutor.
 * Customize thread Pool .
 *
 * @author chenbin sixh
 */
@SuppressWarnings("unused")
public class DelegationThreadPoolExecutor extends ThreadPoolExecutor {

    private final Logger logger = LoggerFactory.getLogger(DelegationThreadPoolExecutor.class);

    /**
     * Define a thread saturation strategy.
     */
    static final RejectedExecutionHandler HANDLER = (r, executor) -> {
        ((DelegationThreadPoolExecutor) executor).onInitialRejection(r);
        BlockingQueue<Runnable> queue = executor.getQueue();
        while (true) {
            if (executor.isShutdown()) {
                throw new RejectedExecutionException("DelegationThreadPoolExecutor Closed");
            }
            try {
                if (queue.offer(r, 1000, TimeUnit.MILLISECONDS)) {
                    break;
                }
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }

        }
    };

    /**
     * Instantiates a new Delegation thread pool executor.
     *
     * @param corePoolSize    the core pool size
     * @param maximumPoolSize the maximum pool size
     * @param keepAliveTime   the keep alive time
     * @param unit            the unit
     * @param workQueue       the work queue
     */
    public DelegationThreadPoolExecutor(final int corePoolSize,
                                        final int maximumPoolSize,
                                        final long keepAliveTime,
                                        final TimeUnit unit,
                                        final BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    /**
     * Instantiates a new Delegation thread pool executor.
     *
     * @param corePoolSize    the core pool size
     * @param maximumPoolSize the maximum pool size
     * @param keepAliveTime   the keep alive time
     * @param unit            the unit
     * @param workQueue       the work queue
     * @param threadFactory   the thread factory
     */
    public DelegationThreadPoolExecutor(final int corePoolSize,
                                        final int maximumPoolSize,
                                        final long keepAliveTime,
                                        final TimeUnit unit,
                                        final BlockingQueue<Runnable> workQueue,
                                        final ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * Instantiates a new Delegation thread pool executor.
     *
     * @param corePoolSize    the core pool size
     * @param maximumPoolSize the maximum pool size
     * @param keepAliveTime   the keep alive time
     * @param unit            the unit
     * @param workQueue       the work queue
     * @param handler         the handler
     */
    public DelegationThreadPoolExecutor(final int corePoolSize,
                                        final int maximumPoolSize,
                                        final long keepAliveTime,
                                        final TimeUnit unit,
                                        final BlockingQueue<Runnable> workQueue,
                                        final RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    /**
     * Instantiates a new Delegation thread pool executor.
     *
     * @param corePoolSize    the core pool size
     * @param maximumPoolSize the maximum pool size
     * @param keepAliveTime   the keep alive time
     * @param unit            the unit
     * @param workQueue       the work queue
     * @param threadFactory   the thread factory
     * @param handler         the handler
     */
    DelegationThreadPoolExecutor(final int corePoolSize,
                                 final int maximumPoolSize,
                                 final long keepAliveTime,
                                 final TimeUnit unit,
                                 final BlockingQueue<Runnable> workQueue,
                                 final ThreadFactory threadFactory,
                                 final RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    /**
     * On initial rejection.
     *
     * @param runnable the runnable
     */
    void onInitialRejection(final Runnable runnable) {
        logger.info("DelegationThreadPoolExecutor:thread {} rejection", runnable);
    }
}
