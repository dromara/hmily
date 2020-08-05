package org.dromara.hmily.core.disruptor.handler;

import org.dromara.hmily.core.concurrent.ConsistentHashSelector;
import org.dromara.hmily.core.disruptor.AbstractDisruptorConsumerExecutor;
import org.dromara.hmily.core.disruptor.DisruptorConsumerFactory;
import org.dromara.hmily.core.repository.HmilyRepositoryDispatcher;
import org.dromara.hmily.core.repository.HmilyRepositoryEvent;

/**
 * this is disruptor consumer.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyRepositoryDataHandler extends AbstractDisruptorConsumerExecutor<HmilyRepositoryEvent> implements DisruptorConsumerFactory<HmilyRepositoryEvent> {
    
    private ConsistentHashSelector executor;
    
    public HmilyRepositoryDataHandler(final ConsistentHashSelector executor) {
        this.executor = executor;
    }
    
    @Override
    public String fixName() {
        return "HmilyRepositoryDataHandler";
    }
    
    @Override
    public AbstractDisruptorConsumerExecutor<HmilyRepositoryEvent> create() {
        return this;
    }
    
    @Override
    public void executor(final HmilyRepositoryEvent event) {
        Long transId = event.getTransId();
        executor.select(String.valueOf(transId)).execute(() -> {
            HmilyRepositoryDispatcher.getInstance().doDispatcher(event);
            event.clear();
        });
       
    }
}
