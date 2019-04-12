package org.dromara.hmily.core.disruptor;

/**
 * DisruptorConsumerFactory.
 * Create a subclass implementation object via the {@link #create()} method,
 * which is called in {@link DisruptorConsumer#onEvent(DataEvent)}.
 *
 * @author chenbin sixh
 */
public interface DisruptorConsumerFactory<T> {

    /**
     * Fix name string.
     *
     * @return the string
     */
    String fixName();

    /**
     * Create disruptor consumer executor.
     *
     * @return the disruptor consumer executor
     */
    DisruptorConsumerExecutor<T> create();
}
