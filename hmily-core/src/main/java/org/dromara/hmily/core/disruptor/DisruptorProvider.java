package org.dromara.hmily.core.disruptor;

import com.lmax.disruptor.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DisruptorProvider.
 * disruptor provider definition.
 *
 * @param <T> the type parameter
 * @author chenbin sixh
 */
public class DisruptorProvider<T> {

    private final RingBuffer<DataEvent<T>> ringBuffer;

    /**
     * The Logger.
     */
    private Logger logger = LoggerFactory.getLogger(DisruptorProvider.class);

    /**
     * Instantiates a new Disruptor provider.
     *
     * @param ringBuffer the ring buffer
     */
    DisruptorProvider(final RingBuffer<DataEvent<T>> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * push data to disruptor queue.
     *
     * @param t the t
     */
    public void onData(final T t) {
        long position = ringBuffer.next();
        try {
            DataEvent<T> de = ringBuffer.get(position);
            de.setT(t);
            ringBuffer.publish(position);
        } catch (Exception ex) {
            logger.error("push data error:", ex);
        }
    }
}
