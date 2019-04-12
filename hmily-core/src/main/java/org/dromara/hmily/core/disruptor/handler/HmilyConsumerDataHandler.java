package org.dromara.hmily.core.disruptor.handler;

import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.enums.EventTypeEnum;
import org.dromara.hmily.core.concurrent.ConsistentHashSelector;
import org.dromara.hmily.core.coordinator.HmilyCoordinatorService;
import org.dromara.hmily.core.disruptor.DisruptorConsumerExecutor;
import org.dromara.hmily.core.disruptor.DisruptorConsumerFactory;
import org.dromara.hmily.core.disruptor.event.HmilyTransactionEvent;

/**
 * this is disruptor consumer.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyConsumerDataHandler extends DisruptorConsumerExecutor<HmilyTransactionEvent> implements DisruptorConsumerFactory {

    private ConsistentHashSelector executor;

    private final HmilyCoordinatorService coordinatorService;

    public HmilyConsumerDataHandler(final ConsistentHashSelector executor, final HmilyCoordinatorService coordinatorService) {
        this.executor = executor;
        this.coordinatorService = coordinatorService;
    }

    @Override
    public String fixName() {
        return "HmilyConsumerDataHandler";
    }

    @Override
    public DisruptorConsumerExecutor create() {
        return this;
    }

    @Override
    public void executor(final HmilyTransactionEvent event) {
        String transId = event.getHmilyTransaction().getTransId();
        executor.select(transId).execute(() -> {
            if (event.getType() == EventTypeEnum.SAVE.getCode()) {
                coordinatorService.save(event.getHmilyTransaction());
            } else if (event.getType() == EventTypeEnum.UPDATE_PARTICIPANT.getCode()) {
                coordinatorService.updateParticipant(event.getHmilyTransaction());
            } else if (event.getType() == EventTypeEnum.UPDATE_STATUS.getCode()) {
                final HmilyTransaction hmilyTransaction = event.getHmilyTransaction();
                coordinatorService.updateStatus(hmilyTransaction.getTransId(), hmilyTransaction.getStatus());
            } else if (event.getType() == EventTypeEnum.DELETE.getCode()) {
                coordinatorService.remove(event.getHmilyTransaction().getTransId());
            }
            event.clear();
        });
    }
}
