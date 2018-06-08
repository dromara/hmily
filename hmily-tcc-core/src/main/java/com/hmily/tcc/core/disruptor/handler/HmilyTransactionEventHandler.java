/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.hmily.tcc.core.disruptor.handler;

import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.enums.EventTypeEnum;
import com.hmily.tcc.core.coordinator.CoordinatorService;
import com.hmily.tcc.core.disruptor.event.HmilyTransactionEvent;
import com.lmax.disruptor.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Disroptor handler.
 * @author xiaoyu(Myth)
 */
@Component
public class HmilyTransactionEventHandler implements EventHandler<HmilyTransactionEvent> {

    @Autowired
    private CoordinatorService coordinatorService;

    @Override
    public void onEvent(final HmilyTransactionEvent hmilyTransactionEvent, final long sequence, final boolean endOfBatch) {
        if (hmilyTransactionEvent.getType() == EventTypeEnum.SAVE.getCode()) {
            coordinatorService.save(hmilyTransactionEvent.getTccTransaction());
        } else if (hmilyTransactionEvent.getType() == EventTypeEnum.UPDATE_PARTICIPANT.getCode()) {
            coordinatorService.updateParticipant(hmilyTransactionEvent.getTccTransaction());
        } else if (hmilyTransactionEvent.getType() == EventTypeEnum.UPDATE_STATUS.getCode()) {
            final TccTransaction tccTransaction = hmilyTransactionEvent.getTccTransaction();
            coordinatorService.updateStatus(tccTransaction.getTransId(), tccTransaction.getStatus());
        } else if (hmilyTransactionEvent.getType() == EventTypeEnum.DELETE.getCode()) {
            coordinatorService.remove(hmilyTransactionEvent.getTccTransaction().getTransId());
        }
        hmilyTransactionEvent.clear();
    }
}
