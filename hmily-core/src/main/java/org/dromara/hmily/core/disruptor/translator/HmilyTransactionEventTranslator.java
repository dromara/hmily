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

package org.dromara.hmily.core.disruptor.translator;

import com.lmax.disruptor.EventTranslatorOneArg;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.core.disruptor.event.HmilyTransactionEvent;

/**
 * EventTranslator.
 * @author xiaoyu(Myth)
 */
public class HmilyTransactionEventTranslator implements EventTranslatorOneArg<HmilyTransactionEvent, HmilyTransaction> {

    private int type;

    public HmilyTransactionEventTranslator(final int type) {
        this.type = type;
    }

    @Override
    public void translateTo(final HmilyTransactionEvent hmilyTransactionEvent, final long l, final HmilyTransaction hmilyTransaction) {
        hmilyTransactionEvent.setHmilyTransaction(hmilyTransaction);
        hmilyTransactionEvent.setType(type);
    }
}
