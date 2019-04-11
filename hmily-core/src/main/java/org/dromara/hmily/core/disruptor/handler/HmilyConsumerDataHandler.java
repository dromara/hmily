package org.dromara.hmily.core.disruptor.handler;

import com.lmax.disruptor.WorkHandler;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.enums.EventTypeEnum;
import org.dromara.hmily.core.concurrent.ConsistentHashSelector;
import org.dromara.hmily.core.coordinator.HmilyCoordinatorService;
import org.dromara.hmily.core.disruptor.event.HmilyTransactionEvent;

/**
 * this is disruptor consumer.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyConsumerDataHandler implements WorkHandler<HmilyTransactionEvent> {

    private ConsistentHashSelector executor;

    private final HmilyCoordinatorService coordinatorService;

    public HmilyConsumerDataHandler(final ConsistentHashSelector executor, final HmilyCoordinatorService coordinatorService) {
        this.executor = executor;
        this.coordinatorService = coordinatorService;
    }

    @Override
    public void onEvent(final HmilyTransactionEvent event) {
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
