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
import java.util.Objects;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.core.service.HmilyTransactionHandlerFactory;
import org.dromara.hmily.spi.HmilySPI;


/**
 * HmilyTransactionFactoryServiceImpl.
 *
 * @author xiaoyu
 */
@HmilySPI("tcc")
public class HmilyTccTransactionHandlerFactory implements HmilyTransactionHandlerFactory {
    
    private static final Map<HmilyRoleEnum, HmilyTransactionHandler> HANDLER_MAP = new EnumMap<>(HmilyRoleEnum.class);
    
    static {
        HANDLER_MAP.put(HmilyRoleEnum.START, new StarterHmilyTccTransactionHandler());
        HANDLER_MAP.put(HmilyRoleEnum.PARTICIPANT, new ParticipantHmilyTccTransactionHandler());
        HANDLER_MAP.put(HmilyRoleEnum.CONSUMER, new ConsumeHmilyTccTransactionHandler());
        HANDLER_MAP.put(HmilyRoleEnum.LOCAL, new LocalHmilyTccTransactionHandler());
    }
    
    /**
     * acquired HmilyTransactionHandler.
     *
     * @param context {@linkplain HmilyTransactionContext}
     * @return Class
     */
    public HmilyTransactionHandler factoryOf(final HmilyTransactionContext context) {
        if (Objects.isNull(context)) {
            return HANDLER_MAP.get(HmilyRoleEnum.START);
        } else {
            //why this code?  because spring cloud invoke has proxy.
            if (context.getRole() == HmilyRoleEnum.SPRING_CLOUD.getCode()) {
                context.setRole(HmilyRoleEnum.START.getCode());
                return HANDLER_MAP.get(HmilyRoleEnum.CONSUMER);
            }
            // if context not null and role is inline  is ParticipantHmilyTransactionHandler.
            if (context.getRole() == HmilyRoleEnum.LOCAL.getCode()) {
                return HANDLER_MAP.get(HmilyRoleEnum.LOCAL);
            } else if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()
                    || context.getRole() == HmilyRoleEnum.START.getCode()) {
                return HANDLER_MAP.get(HmilyRoleEnum.PARTICIPANT);
            }
            return HANDLER_MAP.get(HmilyRoleEnum.CONSUMER);
        }
    }
}
