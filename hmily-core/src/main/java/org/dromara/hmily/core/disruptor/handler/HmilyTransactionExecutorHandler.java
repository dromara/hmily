package org.dromara.hmily.core.disruptor.handler;

import org.dromara.hmily.core.disruptor.AbstractDisruptorConsumerExecutor;
import org.dromara.hmily.core.disruptor.DisruptorConsumerFactory;
import org.dromara.hmily.core.service.HmilyTransactionHandlerAlbum;

/**
 * HmilyTransactionExecutorHandler
 * .
 * About the processing of a rotation function.
 *
 * @author chenbin sixh
 */
public class HmilyTransactionExecutorHandler extends AbstractDisruptorConsumerExecutor<HmilyTransactionHandlerAlbum> implements DisruptorConsumerFactory<HmilyTransactionHandlerAlbum> {
    
    @Override
    public String fixName() {
        return "HmilyTransactionExecutorHandler";
    }
    
    @Override
    public AbstractDisruptorConsumerExecutor<HmilyTransactionHandlerAlbum> create() {
        return this;
    }

    @Override
    public void executor(final HmilyTransactionHandlerAlbum data) {
        data.run();
    }
}

