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

package org.dromara.hmily.core.service;

import java.util.Map;
import java.util.Objects;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.core.context.HmilyTransactionContext;

/**
 * The type Abstract hmily transaction handler.
 *
 * @author xiaoyu
 */
public abstract class AbstractHmilyTransactionHandlerFactory implements HmilyTransactionHandlerFactory {
    
    /**
     * Gets map.
     *
     * @return the map
     */
    protected abstract Map<HmilyRoleEnum, HmilyTransactionHandler> getMap();
    
    @Override
    public HmilyTransactionHandler factoryOf(final HmilyTransactionContext context) {
        if (Objects.isNull(context)) {
            return getMap().get(HmilyRoleEnum.START);
        } else {
            //why this code?  because spring cloud invoke has proxy.
            if (context.getRole() == HmilyRoleEnum.SPRING_CLOUD.getCode()) {
                context.setRole(HmilyRoleEnum.START.getCode());
                return getMap().get(HmilyRoleEnum.CONSUMER);
            }
            // if context not null and role is inline  is ParticipantHmilyTransactionHandler.
            if (context.getRole() == HmilyRoleEnum.LOCAL.getCode()) {
                return getMap().get(HmilyRoleEnum.LOCAL);
            } else if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()
                    || context.getRole() == HmilyRoleEnum.START.getCode()) {
                return getMap().get(HmilyRoleEnum.PARTICIPANT);
            }
            return getMap().get(HmilyRoleEnum.CONSUMER);
        }
    }
}
