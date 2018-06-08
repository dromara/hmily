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
import com.hmily.tcc.core.cache.TccTransactionCacheManager;
import com.hmily.tcc.core.service.HmilyTransactionHandler;
import com.hmily.tcc.core.service.executor.HmilyTransactionExecutor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Participant Handler.
 * @author xiaoyu
 */
@Component
public class ParticipantHmilyTransactionHandler implements HmilyTransactionHandler {

    private final HmilyTransactionExecutor hmilyTransactionExecutor;

    @Autowired
    public ParticipantHmilyTransactionHandler(final HmilyTransactionExecutor hmilyTransactionExecutor) {
        this.hmilyTransactionExecutor = hmilyTransactionExecutor;
    }

    /**
     * 分布式事务提供者处理接口
     * 根据tcc事务上下文的状态来执行相对应的方法.
     *
     * @param point   point 切点
     * @param context context
     * @return Object
     * @throws Throwable 异常
     */
    @Override
    public Object handler(final ProceedingJoinPoint point, final TccTransactionContext context) throws Throwable {
        TccTransaction tccTransaction = null;
        TccTransaction currentTransaction;
        switch (TccActionEnum.acquireByCode(context.getAction())) {
            case TRYING:
                try {
                    //创建事务信息
                    tccTransaction = hmilyTransactionExecutor.beginParticipant(context, point);
                    //发起方法调用
                    final Object proceed = point.proceed();
                    tccTransaction.setStatus(TccActionEnum.TRYING.getCode());
                    //更新日志状态为try 完成
                    hmilyTransactionExecutor.updateStatus(tccTransaction);
                    return proceed;
                } catch (Throwable throwable) {
                    //删除事务日志
                    hmilyTransactionExecutor.deleteTransaction(tccTransaction);
                    throw throwable;
                }
            case CONFIRMING:
                //如果是confirm 通过之前保存的事务信息 进行反射调用
                currentTransaction = TccTransactionCacheManager.getInstance().getTccTransaction(context.getTransId());
                hmilyTransactionExecutor.confirm(currentTransaction);
                break;
            case CANCELING:
                //如果是调用CANCELING 通过之前保存的事务信息 进行反射调用
                currentTransaction = TccTransactionCacheManager.getInstance().getTccTransaction(context.getTransId());
                hmilyTransactionExecutor.cancel(currentTransaction);
                break;
            default:
                break;
        }
        Method method = ((MethodSignature) (point.getSignature())).getMethod();
        return getDefaultValue(method.getReturnType());
    }

    private Object getDefaultValue(final Class type) {
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
