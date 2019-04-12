package org.dromara.hmily.core.disruptor;

import com.lmax.disruptor.WorkHandler;

/**
 * DisruptorConsumer.
 * disruptor consumer work handler.
 * @author chenbin sixh
 */
public class DisruptorConsumer<T> implements WorkHandler<DataEvent<T>> {

    private DisruptorConsumerFactory<T> factory;

    DisruptorConsumer(final DisruptorConsumerFactory<T> factory) {
        this.factory = factory;
    }

    @Override
    public void onEvent(final DataEvent<T> t) {
        if (t != null) {
            factory.create().executor(t.getT());
        }
    }
}
