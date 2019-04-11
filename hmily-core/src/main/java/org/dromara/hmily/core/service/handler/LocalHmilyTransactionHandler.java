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

package org.dromara.hmily.core.service.handler;

import org.dromara.hmily.common.utils.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.bean.entity.HmilyInvocation;
import org.dromara.hmily.common.bean.entity.HmilyParticipant;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.core.service.executor.HmilyTransactionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * InlineHmilyTransactionHandler.
 * This is the method annotated by TCC within an actor.
 *
 * @author xiaoyu
 */
@Component
public class LocalHmilyTransactionHandler implements HmilyTransactionHandler {

    private final HmilyTransactionExecutor hmilyTransactionExecutor;

    /**
     * Instantiates a new Local hmily transaction handler.
     *
     * @param hmilyTransactionExecutor the hmily transaction executor
     */
    @Autowired
    public LocalHmilyTransactionHandler(final HmilyTransactionExecutor hmilyTransactionExecutor) {
        this.hmilyTransactionExecutor = hmilyTransactionExecutor;
    }

    @Override
    public Object handler(final ProceedingJoinPoint point, final HmilyTransactionContext context) throws Throwable {
        if (HmilyActionEnum.TRYING.getCode() == context.getAction()) {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Class<?> clazz = point.getTarget().getClass();
            Object[] args = point.getArgs();
            final Hmily hmily = method.getAnnotation(Hmily.class);
            HmilyInvocation confirmInvocation = null;
            String confirmMethodName = hmily.confirmMethod();
            String cancelMethodName = hmily.cancelMethod();
            if (StringUtils.isNoneBlank(confirmMethodName)) {
                confirmInvocation = new HmilyInvocation(clazz, confirmMethodName, method.getParameterTypes(), args);
            }
            HmilyInvocation cancelInvocation = null;
            if (StringUtils.isNoneBlank(cancelMethodName)) {
                cancelInvocation = new HmilyInvocation(clazz, cancelMethodName, method.getParameterTypes(), args);
            }
            final HmilyParticipant hmilyParticipant = new HmilyParticipant(context.getTransId(),
                    confirmInvocation, cancelInvocation);
            hmilyTransactionExecutor.registerByNested(context.getTransId(), hmilyParticipant);
        }
        return point.proceed();
    }

}
