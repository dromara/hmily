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
package com.hmily.tcc.core.interceptor;


import com.hmily.tcc.annotation.Tcc;
import com.hmily.tcc.annotation.TccPatternEnum;
import com.hmily.tcc.common.enums.TccActionEnum;
import com.hmily.tcc.common.bean.entity.Participant;
import com.hmily.tcc.common.bean.entity.TccInvocation;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.core.service.handler.TccTransactionManager;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;


/**
 * @author xiaoyu
 */
@Component
public class TccCoordinatorMethodInterceptor {


    private final TccTransactionManager tccTransactionManager;

    @Autowired
    public TccCoordinatorMethodInterceptor(TccTransactionManager tccTransactionManager) {
        this.tccTransactionManager = tccTransactionManager;
    }


    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {

        final TccTransaction currentTransaction = tccTransactionManager.getCurrentTransaction();

        if (Objects.nonNull(currentTransaction)) {
            final TccActionEnum action = TccActionEnum.acquireByCode(currentTransaction.getStatus());
            switch (action) {
                case PRE_TRY:
                    registerParticipant(pjp, currentTransaction.getTransId());
                    break;
                case TRYING:
                    break;
                case CONFIRMING:
                    break;
                case CANCELING:
                    break;
                default:
                    break;
            }
        }
        return pjp.proceed(pjp.getArgs());
    }


    /**
     * 获取调用接口的协调方法并封装
     *
     * @param point 切点
     */
    private void registerParticipant(ProceedingJoinPoint point, String transId) throws NoSuchMethodException {

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        Class<?> clazz = point.getTarget().getClass();

        Object[] args = point.getArgs();

        final Tcc tcc = method.getAnnotation(Tcc.class);

        //获取协调方法
        String confirmMethodName = tcc.confirmMethod();

        String cancelMethodName = tcc.cancelMethod();

        //设置模式
        final TccPatternEnum pattern = tcc.pattern();

        tccTransactionManager.getCurrentTransaction().setPattern(pattern.getCode());


        TccInvocation confirmInvocation = null;
        if (StringUtils.isNoneBlank(confirmMethodName)) {
            confirmInvocation = new TccInvocation(clazz,
                    confirmMethodName, method.getParameterTypes(), args);
        }

        TccInvocation cancelInvocation = null;
        if (StringUtils.isNoneBlank(cancelMethodName)) {
            cancelInvocation = new TccInvocation(clazz,
                    cancelMethodName,
                    method.getParameterTypes(), args);
        }


        //封装调用点
        final Participant participant = new Participant(
                transId,
                confirmInvocation,
                cancelInvocation);

        tccTransactionManager.enlistParticipant(participant);

    }
}
