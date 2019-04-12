package org.dromara.hmily.core.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * DisruptorEventFactory.
 * disruptor Create a factory implementation of the object.
 * @author chenbin sixh
 */
public class DisruptorEventFactory<T> implements EventFactory<DataEvent<T>> {
    @Override
    public DataEvent<T> newInstance() {
        return new DataEvent<>();
    }
}
