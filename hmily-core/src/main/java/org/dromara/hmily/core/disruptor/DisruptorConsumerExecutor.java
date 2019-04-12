package org.dromara.hmily.core.disruptor;

import java.util.HashSet;
import java.util.Set;

/**
 * DisruptorConsumerExecutor.
 * disruptor consumer executor.
 *
 * @param <T> the type parameter
 * @author chenbin sixh
 */
public abstract class DisruptorConsumerExecutor<T> {

    /**
     * Recorded the subscription processing after the user needs to subscribe to the calculation result.
     */
    private Set<ExecutorSubscriber> subscribers = new HashSet<>();

    /**
     * Add subscribers disruptor consumer executor.
     *
     * @param subscriber subscriber；
     * @return the disruptor consumer executor
     */
    public DisruptorConsumerExecutor addSubscribers(final ExecutorSubscriber subscriber) {
        subscribers.add(subscriber);
        return this;
    }

    /**
     * Add subscribers disruptor consumer executor.
     *
     * @param subscribers the subscribers
     * @return the disruptor consumer executor
     */
    public DisruptorConsumerExecutor addSubscribers(final Set<ExecutorSubscriber> subscribers) {
        subscribers.forEach(this::addSubscribers);
        return this;
    }

    /**
     * Gets subscribers.
     *
     * @return the subscribers
     */
    public Set<ExecutorSubscriber> getSubscribers() {
        return subscribers;
    }

    /**
     * Perform the processing of the current event.
     *
     * @param data the data
     */
    public abstract void executor(T data);
}
