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

import com.hmily.tcc.common.enums.TccActionEnum;
import com.hmily.tcc.common.bean.context.TccTransactionContext;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.core.service.TccTransactionHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author xiaoyu
 */
@Component
public class ProviderTccTransactionHandler implements TccTransactionHandler {


    private final TccTransactionManager tccTransactionManager;

    @Autowired
    public ProviderTccTransactionHandler(TccTransactionManager tccTransactionManager) {
        this.tccTransactionManager = tccTransactionManager;
    }


    /**
     * 分布式事务提供者处理接口
     * 根据tcc事务上下文的状态来执行相对应的方法
     *
     * @param point   point 切点
     * @param context context
     * @return Object
     * @throws Throwable 异常
     */
    @Override
    public Object handler(ProceedingJoinPoint point, TccTransactionContext context) throws Throwable {
        TccTransaction tccTransaction = null;
        try {
            switch (TccActionEnum.acquireByCode(context.getAction())) {
                case TRYING:
                    try {
                        //创建事务信息
                        tccTransaction = tccTransactionManager.providerBegin(context, point);
                        //发起方法调用
                        final Object proceed = point.proceed();

                        tccTransactionManager.updateStatus(tccTransaction.getTransId(),
                                TccActionEnum.TRYING.getCode());
                        return proceed;
                    } catch (Throwable throwable) {
                        tccTransactionManager.removeTccTransaction(tccTransaction);
                        throw throwable;

                    }
                case CONFIRMING:
                    //如果是confirm 通过之前保存的事务信息 进行反射调用
                    tccTransactionManager.acquire(context);
                    tccTransactionManager.confirm();
                    break;
                case CANCELING:
                    //如果是调用CANCELING 通过之前保存的事务信息 进行反射调用
                    tccTransactionManager.acquire(context);
                    tccTransactionManager.cancel();
                    break;
                default:
                    break;
            }
        } finally {
            tccTransactionManager.remove();
        }
        Method method = ((MethodSignature) (point.getSignature())).getMethod();
        return getDefaultValue(method.getReturnType());
    }

    private Object getDefaultValue(Class type) {

        if (boolean.class.equals(type)) {
            return false;
        } else if (byte.class.equals(type)) {
            return 0;
        } else if (short.class.equals(type)) {
            return 0;
        } else if (int.class.equals(type)) {
            return 0;
        } else if (long.class.equals(type)) {
            return 0;
        } else if (float.class.equals(type)) {
            return 0;
        } else if (double.class.equals(type)) {
            return 0;
        }

        return null;
    }
}
