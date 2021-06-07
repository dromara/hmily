/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.tcc.handler;

import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.core.service.AbstractHmilyTransactionHandlerRegistry;
import org.dromara.hmily.spi.HmilySPI;


/**
 * Hmily Tcc transaction handler registry.
 *
 * @author xiaoyu
 */
@HmilySPI("tcc")
public class HmilyTccTransactionHandlerRegistry extends AbstractHmilyTransactionHandlerRegistry {
    
    @Override
    public void register() {
        getHandlers().put(HmilyRoleEnum.START, new StarterHmilyTccTransactionHandler());
        getHandlers().put(HmilyRoleEnum.PARTICIPANT, new ParticipantHmilyTccTransactionHandler());
        getHandlers().put(HmilyRoleEnum.CONSUMER, new ConsumeHmilyTccTransactionHandler());
        getHandlers().put(HmilyRoleEnum.LOCAL, new LocalHmilyTccTransactionHandler());
    }
}
