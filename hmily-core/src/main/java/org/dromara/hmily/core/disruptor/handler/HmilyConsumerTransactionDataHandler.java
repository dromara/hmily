package org.dromara.hmily.core.disruptor.handler;

import org.dromara.hmily.core.disruptor.DisruptorConsumerExecutor;
import org.dromara.hmily.core.disruptor.DisruptorConsumerFactory;
import org.dromara.hmily.core.service.handler.TransactionHandlerAlbum;

/**
 * HmilyTransactionHandler.
 * About the processing of a rotation function.
 *
 * @author chenbin sixh
 */
public class HmilyConsumerTransactionDataHandler extends DisruptorConsumerExecutor<TransactionHandlerAlbum> implements DisruptorConsumerFactory {


    @Override
    public String fixName() {
        return "HmilyConsumerTransactionDataHandler";
    }

    @Override
    public DisruptorConsumerExecutor create() {
        return this;
    }

    @Override
    public void executor(final TransactionHandlerAlbum data) {
        data.run();
    }
}

