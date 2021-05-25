package org.dromara.hmily.core.disruptor.handler;

import org.dromara.hmily.core.disruptor.HmilyDisruptorConsumer;
import org.dromara.hmily.core.service.HmilyTransactionTask;

/**
 * Hmily transaction event consumer.
 * .
 * About the processing of a rotation function.
 *
 * @author chenbin sixh
 */
public class HmilyTransactionEventConsumer implements HmilyDisruptorConsumer<HmilyTransactionTask> {
    
    @Override
    public String fixName() {
        return "HmilyTransactionEventConsumer";
    }
    
    @Override
    public void execute(final HmilyTransactionTask task) {
        task.run();
    }
}

