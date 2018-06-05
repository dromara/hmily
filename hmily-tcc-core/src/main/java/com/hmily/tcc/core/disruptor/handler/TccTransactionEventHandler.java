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
import com.hmily.tcc.common.enums.CoordinatorActionEnum;
import com.hmily.tcc.common.enums.EventTypeEnum;
import com.hmily.tcc.core.coordinator.CoordinatorService;
import com.hmily.tcc.core.disruptor.event.TccTransactionEvent;
import com.lmax.disruptor.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2018/3/5 11:52
 * @since JDK 1.8
 */
@Component
public class TccTransactionEventHandler implements EventHandler<TccTransactionEvent> {

    @Autowired
    private CoordinatorService coordinatorService;


    @Override
    public void onEvent(TccTransactionEvent tccTransactionEvent,
                        long sequence, boolean endOfBatch) {
        if (tccTransactionEvent.getType() == EventTypeEnum.SAVE.getCode()) {
            coordinatorService.save(tccTransactionEvent.getTccTransaction());
        } else if (tccTransactionEvent.getType() == EventTypeEnum.UPDATE_PARTICIPANT.getCode()) {
            coordinatorService.updateParticipant(tccTransactionEvent.getTccTransaction());
        } else if (tccTransactionEvent.getType() == EventTypeEnum.UPDATE_STATUS.getCode()) {
            final TccTransaction tccTransaction = tccTransactionEvent.getTccTransaction();
            coordinatorService.updateStatus(tccTransaction.getTransId(), tccTransaction.getStatus());
        } else if (tccTransactionEvent.getType() == EventTypeEnum.DELETE.getCode()) {
            coordinatorService.remove(tccTransactionEvent.getTccTransaction().getTransId());
        }
        tccTransactionEvent.clear();
    }
}
