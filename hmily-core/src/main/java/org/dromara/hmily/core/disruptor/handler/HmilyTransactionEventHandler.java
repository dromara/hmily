package org.dromara.hmily.core.disruptor.handler;

import org.dromara.hmily.core.disruptor.AbstractDisruptorConsumerExecutor;
import org.dromara.hmily.core.disruptor.DisruptorConsumerFactory;
import org.dromara.hmily.core.service.HmilyTransactionHandlerAlbum;

/**
 * Hmily transaction event consume handler.
 * .
 * About the processing of a rotation function.
 *
 * @author chenbin sixh
 */
public class HmilyTransactionEventHandler extends AbstractDisruptorConsumerExecutor<HmilyTransactionHandlerAlbum> implements DisruptorConsumerFactory<HmilyTransactionHandlerAlbum> {
    
    @Override
    public String fixName() {
        return "HmilyTransactionEventHandler";
    }
    
    @Override
    public AbstractDisruptorConsumerExecutor<HmilyTransactionHandlerAlbum> create() {
        return this;
    }

    @Override
    public void execute(final HmilyTransactionHandlerAlbum data) {
        data.run();
    }
}

