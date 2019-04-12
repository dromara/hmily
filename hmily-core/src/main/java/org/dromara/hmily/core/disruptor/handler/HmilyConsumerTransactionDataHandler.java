package org.dromara.hmily.core.disruptor.handler;

import org.dromara.hmily.core.disruptor.AbstractDisruptorConsumerExecutor;
import org.dromara.hmily.core.disruptor.DisruptorConsumerFactory;
import org.dromara.hmily.core.service.HmilyTransactionHandlerAlbum;

/**
 * HmilyTransactionHandler.
 * About the processing of a rotation function.
 *
 * @author chenbin sixh
 */
public class HmilyConsumerTransactionDataHandler extends AbstractDisruptorConsumerExecutor<HmilyTransactionHandlerAlbum> implements DisruptorConsumerFactory {


    @Override
    public String fixName() {
        return "HmilyConsumerTransactionDataHandler";
    }

    @Override
    public AbstractDisruptorConsumerExecutor create() {
        return this;
    }

    @Override
    public void executor(final HmilyTransactionHandlerAlbum data) {
        data.run();
    }
}

