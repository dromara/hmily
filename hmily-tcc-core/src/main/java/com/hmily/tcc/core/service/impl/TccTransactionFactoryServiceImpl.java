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
import com.hmily.tcc.core.service.TccTransactionFactoryService;
import com.hmily.tcc.core.service.handler.ConsumeTccTransactionHandler;
import com.hmily.tcc.core.service.handler.ProviderTccTransactionHandler;
import com.hmily.tcc.core.service.handler.StartTccTransactionHandler;
import com.hmily.tcc.core.service.handler.TccTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;


/**
 * @author xiaoyu
 */
@Service("tccTransactionFactoryService")
public class TccTransactionFactoryServiceImpl implements TccTransactionFactoryService {


    private final TccTransactionManager tccTransactionManager;

    @Autowired
    public TccTransactionFactoryServiceImpl(TccTransactionManager tccTransactionManager) {
        this.tccTransactionManager = tccTransactionManager;
    }


    /**
     * 返回 实现TxTransactionHandler类的名称
     *
     * @param context tcc事务上下文
     * @return Class<T>
     * @throws Throwable 抛出异常
     */
    @Override
    public Class factoryOf(TccTransactionContext context) throws Throwable {

        //如果事务还没开启或者 tcc事务上下文是空， 那么应该进入发起调用
        if (!tccTransactionManager.isBegin() && Objects.isNull(context)) {
            return StartTccTransactionHandler.class;
        } else if (tccTransactionManager.isBegin() && Objects.isNull(context)) {
            return ConsumeTccTransactionHandler.class;
        } else if (Objects.nonNull(context)) {
            return ProviderTccTransactionHandler.class;
        }
        return ConsumeTccTransactionHandler.class;
    }
}
