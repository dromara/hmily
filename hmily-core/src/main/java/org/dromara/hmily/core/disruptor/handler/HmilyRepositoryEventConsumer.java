package org.dromara.hmily.core.disruptor.handler;

import org.dromara.hmily.core.concurrent.ConsistentHashSelector;
import org.dromara.hmily.core.disruptor.HmilyDisruptorConsumer;
import org.dromara.hmily.core.repository.HmilyRepositoryEvent;
import org.dromara.hmily.core.repository.HmilyRepositoryEventDispatcher;

/**
 * Hmily repository event consumer.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyRepositoryEventConsumer implements HmilyDisruptorConsumer<HmilyRepositoryEvent> {
    
    private ConsistentHashSelector executor;
    
    public HmilyRepositoryEventConsumer(final ConsistentHashSelector executor) {
        this.executor = executor;
    }
    
    @Override
    public String fixName() {
        return "HmilyRepositoryEventConsumer";
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
