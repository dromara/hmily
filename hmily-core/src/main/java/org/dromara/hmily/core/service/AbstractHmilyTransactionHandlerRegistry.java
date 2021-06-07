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

package org.dromara.hmily.core.service;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.core.context.HmilyTransactionContext;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * The type Abstract hmily transaction handler.
 *
 * @author xiaoyu
 */
public abstract class AbstractHmilyTransactionHandlerRegistry implements HmilyTransactionHandlerRegistry {
    
    @Getter
    private final Map<HmilyRoleEnum, HmilyTransactionHandler> handlers = new EnumMap<>(HmilyRoleEnum.class);
    
    public AbstractHmilyTransactionHandlerRegistry() {
        register();
    }
    
    protected abstract void register();
    
    @Override
    public HmilyTransactionHandler select(final HmilyTransactionContext context) {
        if (Objects.isNull(context)) {
            return getHandler(HmilyRoleEnum.START);
        } else {
            // if context not null and role is inline  is ParticipantHmilyTransactionHandler.
            if (context.getRole() == HmilyRoleEnum.LOCAL.getCode()) {
                return getHandler(HmilyRoleEnum.LOCAL);
            } else if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()
                    || context.getRole() == HmilyRoleEnum.START.getCode()) {
                return getHandler(HmilyRoleEnum.PARTICIPANT);
            }
            return getHandler(HmilyRoleEnum.CONSUMER);
        }
    }
    
    private HmilyTransactionHandler getHandler(final HmilyRoleEnum role) {
        Preconditions.checkState(handlers.containsKey(role));
        return handlers.get(role);
    }
}
