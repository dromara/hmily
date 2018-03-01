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
import com.hmily.tcc.core.service.TccTransactionAspectService;
import com.hmily.tcc.core.service.TccTransactionFactoryService;
import com.hmily.tcc.core.service.TccTransactionHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author xiaoyu
 */
@Service("tccTransactionAspectService")
@SuppressWarnings("unchecked")
public class TccTransactionAspectServiceImpl implements TccTransactionAspectService {

    private final TccTransactionFactoryService tccTransactionFactoryService;

    @Autowired
    public TccTransactionAspectServiceImpl(TccTransactionFactoryService tccTransactionFactoryService) {
        this.tccTransactionFactoryService = tccTransactionFactoryService;
    }

    /**
     * tcc 事务切面服务
     *
     * @param tccTransactionContext tcc事务上下文对象
     * @param point                 切点
     * @return object
     * @throws Throwable 异常信息
     */
    @Override
    public Object invoke(TccTransactionContext tccTransactionContext, ProceedingJoinPoint point) throws Throwable {
        final Class aClass = tccTransactionFactoryService.factoryOf(tccTransactionContext);
        final TccTransactionHandler txTransactionHandler =
                (TccTransactionHandler) SpringBeanUtils.getInstance().getBean(aClass);
        return txTransactionHandler.handler(point, tccTransactionContext);
    }
}
