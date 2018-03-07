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

package com.hmily.tcc.core.disruptor.translator;

import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.enums.CoordinatorActionEnum;
import com.hmily.tcc.core.disruptor.event.TccTransactionEvent;
import com.lmax.disruptor.EventTranslatorOneArg;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2018/3/5 11:54
 * @since JDK 1.8
 */
public class TccTransactionEventTranslator implements EventTranslatorOneArg<TccTransactionEvent, TccTransaction> {

    private int type;

    public TccTransactionEventTranslator(int type) {
        this.type = type;
    }

    @Override
    public void translateTo(TccTransactionEvent tccTransactionEvent, long l,
                            TccTransaction tccTransaction) {
        tccTransactionEvent.setTccTransaction(tccTransaction);
        tccTransactionEvent.setType(type);
    }
}
