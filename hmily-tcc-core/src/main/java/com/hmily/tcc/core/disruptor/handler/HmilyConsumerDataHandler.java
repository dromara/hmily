package com.hmily.tcc.core.disruptor.handler;

import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.enums.EventTypeEnum;
import com.hmily.tcc.core.coordinator.CoordinatorService;
import com.hmily.tcc.core.disruptor.event.HmilyTransactionEvent;
import com.lmax.disruptor.WorkHandler;

import java.util.concurrent.Executor;

/**
 * this is disruptor consumer.
 * @author xiaoyu(Myth)
 */
public class HmilyConsumerDataHandler implements WorkHandler<HmilyTransactionEvent> {

    private Executor executor;

    private final CoordinatorService coordinatorService;

    public HmilyConsumerDataHandler(final Executor executor, final CoordinatorService coordinatorService) {
        this.executor = executor;
        this.coordinatorService = coordinatorService;
    }

    @Override
    public void onEvent(final HmilyTransactionEvent event) {
        executor.execute(() -> {
            if (event.getType() == EventTypeEnum.SAVE.getCode()) {
                coordinatorService.save(event.getTccTransaction());
            } else if (event.getType() == EventTypeEnum.UPDATE_PARTICIPANT.getCode()) {
                coordinatorService.updateParticipant(event.getTccTransaction());
            } else if (event.getType() == EventTypeEnum.UPDATE_STATUS.getCode()) {
                final TccTransaction tccTransaction = event.getTccTransaction();
                coordinatorService.updateStatus(tccTransaction.getTransId(), tccTransaction.getStatus());
            } else if (event.getType() == EventTypeEnum.DELETE.getCode()) {
                coordinatorService.remove(event.getTccTransaction().getTransId());
            }
            event.clear();
        });
    }
}
