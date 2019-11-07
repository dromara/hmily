package org.dromara.hmily.core.disruptor.handler;

import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.enums.EventTypeEnum;
import org.dromara.hmily.core.concurrent.ConsistentHashSelector;
import org.dromara.hmily.core.coordinator.HmilyCoordinatorService;
import org.dromara.hmily.core.disruptor.AbstractDisruptorConsumerExecutor;
import org.dromara.hmily.core.disruptor.DisruptorConsumerFactory;
import org.dromara.hmily.core.disruptor.event.HmilyTransactionEvent;

/**
 * this is disruptor consumer.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyConsumerLogDataHandler extends AbstractDisruptorConsumerExecutor<HmilyTransactionEvent> implements DisruptorConsumerFactory {

    private ConsistentHashSelector executor;

    private final HmilyCoordinatorService coordinatorService;

    public HmilyConsumerLogDataHandler(final ConsistentHashSelector executor, final HmilyCoordinatorService coordinatorService) {
        this.executor = executor;
        this.coordinatorService = coordinatorService;
    }

    @Override
    public String fixName() {
        return "HmilyConsumerDataHandler";
    }

    @Override
    public AbstractDisruptorConsumerExecutor create() {
        return this;
    }

    @Override
    public void executor(final HmilyTransactionEvent event) {
        String transId = event.getHmilyTransaction().getTransId();
        executor.select(transId).execute(() -> {
            EventTypeEnum eventTypeEnum = EventTypeEnum.buildByCode(event.getType());
            switch (eventTypeEnum) {
                case SAVE:
                    coordinatorService.save(event.getHmilyTransaction());
                    break;
                case DELETE:
                    coordinatorService.remove(event.getHmilyTransaction().getTransId());
                    break;
                case UPDATE_STATUS:
                    final HmilyTransaction hmilyTransaction = event.getHmilyTransaction();
                    coordinatorService.updateStatus(hmilyTransaction.getTransId(), hmilyTransaction.getStatus());
                    break;
                case UPDATE_PARTICIPANT:
                    coordinatorService.updateParticipant(event.getHmilyTransaction());
                    break;
                default:
                    break;
            }
            event.clear();
        });
    }
}
