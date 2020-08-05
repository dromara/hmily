/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.EnumMap;
import java.util.Map;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.core.service.AbstractHmilyTransactionHandlerFactory;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.spi.HmilySPI;


/**
 * HmilyTransactionFactoryServiceImpl.
 *
 * @author xiaoyu
 */
@HmilySPI("tcc")
public class HmilyTccTransactionHandlerFactory extends AbstractHmilyTransactionHandlerFactory {
    
    private static final Map<HmilyRoleEnum, HmilyTransactionHandler> HANDLER_MAP = new EnumMap<>(HmilyRoleEnum.class);
    
    static {
        HANDLER_MAP.put(HmilyRoleEnum.START, new StarterHmilyTccTransactionHandler());
        HANDLER_MAP.put(HmilyRoleEnum.PARTICIPANT, new ParticipantHmilyTccTransactionHandler());
        HANDLER_MAP.put(HmilyRoleEnum.CONSUMER, new ConsumeHmilyTccTransactionHandler());
        HANDLER_MAP.put(HmilyRoleEnum.LOCAL, new LocalHmilyTccTransactionHandler());
    }
    
    @Override
    protected Map<HmilyRoleEnum, HmilyTransactionHandler> getMap() {
        return HANDLER_MAP;
    }
}
