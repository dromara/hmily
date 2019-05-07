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

package org.dromara.hmily.core.service.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.core.helper.SpringBeanUtils;
import org.dromara.hmily.core.service.HmilyTransactionAspectService;
import org.dromara.hmily.core.service.HmilyTransactionFactoryService;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * HmilyTransactionAspectServiceImpl.
 *
 * @author xiaoyu
 */
@Service("hmilyTransactionAspectService")
@SuppressWarnings("unchecked")
public class HmilyTransactionAspectServiceImpl implements HmilyTransactionAspectService {

    private final HmilyTransactionFactoryService hmilyTransactionFactoryService;

    /**
     * Instantiates a new Hmily transaction aspect service.
     *
     * @param hmilyTransactionFactoryService the hmily transaction factory service
     */
    @Autowired
    public HmilyTransactionAspectServiceImpl(final HmilyTransactionFactoryService hmilyTransactionFactoryService) {
        this.hmilyTransactionFactoryService = hmilyTransactionFactoryService;
    }

    /**
     * hmily transaction aspect.
     *
     * @param hmilyTransactionContext {@linkplain  HmilyTransactionContext}
     * @param point                   {@linkplain ProceedingJoinPoint}
     * @return object  return value
     * @throws Throwable exception
     */
    @Override
    public Object invoke(final HmilyTransactionContext hmilyTransactionContext, final ProceedingJoinPoint point) throws Throwable {
        final Class clazz = hmilyTransactionFactoryService.factoryOf(hmilyTransactionContext);
        final HmilyTransactionHandler txTransactionHandler =
                (HmilyTransactionHandler) SpringBeanUtils.getInstance().getBean(clazz);
        return txTransactionHandler.handler(point, hmilyTransactionContext);
    }
}
