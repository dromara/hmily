package org.dromara.hmily.core.disruptor.handler;

import org.dromara.hmily.core.concurrent.ConsistentHashSelector;
import org.dromara.hmily.core.disruptor.AbstractDisruptorConsumerExecutor;
import org.dromara.hmily.core.disruptor.DisruptorConsumerFactory;
import org.dromara.hmily.core.repository.HmilyRepositoryEvent;
import org.dromara.hmily.core.repository.HmilyRepositoryEventDispatcher;

/**
 * this is disruptor consumer.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyRepositoryEventHandler extends AbstractDisruptorConsumerExecutor<HmilyRepositoryEvent> implements DisruptorConsumerFactory<HmilyRepositoryEvent> {
    
    private ConsistentHashSelector executor;
    
    public HmilyRepositoryEventHandler(final ConsistentHashSelector executor) {
        this.executor = executor;
    }
    
    @Override
    public String fixName() {
        return "HmilyRepositoryEventHandler";
    }
    
    @Override
    public AbstractDisruptorConsumerExecutor<HmilyRepositoryEvent> create() {
        return this;
    }
    
    @Override
    public void execute(final HmilyRepositoryEvent event) {
        Long transId = event.getTransId();
        executor.select(String.valueOf(transId)).execute(() -> {
            HmilyRepositoryEventDispatcher.getInstance().doDispatch(event);
            event.clear();
        });
       
    }
}
