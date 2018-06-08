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
import com.hmily.tcc.core.helper.SpringBeanUtils;
import com.hmily.tcc.core.service.HmilyTransactionAspectService;
import com.hmily.tcc.core.service.HmilyTransactionFactoryService;
import com.hmily.tcc.core.service.HmilyTransactionHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * HmilyTransactionAspectServiceImpl.
 * @author xiaoyu
 */
@Service("tccTransactionAspectService")
@SuppressWarnings("unchecked")
public class HmilyTransactionAspectServiceImpl implements HmilyTransactionAspectService {

    private final HmilyTransactionFactoryService hmilyTransactionFactoryService;

    @Autowired
    public HmilyTransactionAspectServiceImpl(final HmilyTransactionFactoryService hmilyTransactionFactoryService) {
        this.hmilyTransactionFactoryService = hmilyTransactionFactoryService;
    }

    /**
     * tcc 事务切面服务.
     *
     * @param tccTransactionContext tcc事务上下文对象
     * @param point                 切点
     * @return object
     * @throws Throwable 异常信息
     */
    @Override
    public Object invoke(final TccTransactionContext tccTransactionContext, final ProceedingJoinPoint point) throws Throwable {
        final Class clazz = hmilyTransactionFactoryService.factoryOf(tccTransactionContext);
        final HmilyTransactionHandler txTransactionHandler = (HmilyTransactionHandler) SpringBeanUtils.getInstance().getBean(clazz);
        return txTransactionHandler.handler(point, tccTransactionContext);
    }
}
