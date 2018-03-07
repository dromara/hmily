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

package com.hmily.tcc.core.service.handler;

import com.hmily.tcc.common.bean.context.TccTransactionContext;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.enums.TccActionEnum;
import com.hmily.tcc.core.service.TccTransactionHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author xiaoyu
 */
@Component
public class StartTccTransactionHandler implements TccTransactionHandler {


    private final TccTransactionManager tccTransactionManager;


    private static final Lock LOCK = new ReentrantLock();

    @Autowired
    public StartTccTransactionHandler(TccTransactionManager tccTransactionManager) {
        this.tccTransactionManager = tccTransactionManager;
    }

    /**
     * 分布式事务处理接口
     *
     * @param point   point 切点
     * @param context 信息
     * @return Object
     * @throws Throwable 异常
     */
    @Override
    public Object handler(ProceedingJoinPoint point, TccTransactionContext context) throws Throwable {
        Object returnValue;
        try {
            final TccTransaction tccTransaction = tccTransactionManager.begin(point);
            try {
                //发起调用 执行try方法
                returnValue = point.proceed();

                tccTransaction.setStatus(TccActionEnum.TRYING.getCode());

                tccTransactionManager.updateStatus(tccTransaction);

            } catch (Throwable throwable) {
                //异常执行cancel
                tccTransactionManager.cancel(tccTransactionManager.getCurrentTransaction());

                throw throwable;
            }
            //try成功执行confirm confirm 失败的话，那就只能走本地补偿
            tccTransactionManager.confirm(tccTransactionManager.getCurrentTransaction());
        } finally {
            tccTransactionManager.remove();
        }
        return returnValue;
    }
}
