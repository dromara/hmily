package org.dromara.hmily.core.disruptor;

import java.util.Collection;

/**
 * The interface Executor subscriber.
 *
 * @param <T> the type parameter
 * @author chenbin sixh
 */
public interface ExecutorSubscriber<T> {

    /**
     * Executor.
     *
     * @param collections the collections
     */
    void executor(Collection<? extends T> collections);
}
