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

package com.hmily.tcc.core.service.impl;

import com.hmily.tcc.common.bean.context.TccTransactionContext;
import com.hmily.tcc.common.enums.TccRoleEnum;
import com.hmily.tcc.core.service.HmilyTransactionFactoryService;
import com.hmily.tcc.core.service.handler.ConsumeHmilyTransactionHandler;
import com.hmily.tcc.core.service.handler.LocalHmilyTransactionHandler;
import com.hmily.tcc.core.service.handler.ParticipantHmilyTransactionHandler;
import com.hmily.tcc.core.service.handler.StarterHmilyTransactionHandler;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * HmilyTransactionFactoryServiceImpl.
 *
 * @author xiaoyu
 */
@Service("tccTransactionFactoryService")
public class HmilyTransactionFactoryServiceImpl implements HmilyTransactionFactoryService {

    /**
     * acquired HmilyTransactionHandler.
     *
     * @param context {@linkplain TccTransactionContext}
     * @return Class
     */
    @Override
    public Class factoryOf(final TccTransactionContext context) {
        if (Objects.isNull(context)) {
            return StarterHmilyTransactionHandler.class;
        } else {
            // if context not null and role is inline  is ParticipantHmilyTransactionHandler.
            if (context.getRole() == TccRoleEnum.LOCAL.getCode()) {
                return LocalHmilyTransactionHandler.class;
            } else if (context.getRole() == TccRoleEnum.START.getCode()
                    || context.getRole() == TccRoleEnum.INLINE.getCode()) {
                return ParticipantHmilyTransactionHandler.class;
            }
            return ConsumeHmilyTransactionHandler.class;
        }
    }
}
