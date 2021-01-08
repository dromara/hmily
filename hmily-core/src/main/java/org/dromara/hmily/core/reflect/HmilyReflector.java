/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.dromara.hmily.core.reflect;

import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.dromara.hmily.common.enums.ExecutorTypeEnum;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.core.context.*;
import org.dromara.hmily.core.holder.SingletonHolder;
import org.dromara.hmily.core.provide.ObjectProvide;
import org.dromara.hmily.repository.spi.entity.HmilyInvocation;
import org.dromara.hmily.repository.spi.entity.HmilyInvocationWithContext;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;

/**
 * The type Hmily reflector.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyReflector {

    /**
     * 获取上下文参数并设置
     */
    private static boolean setContextParams(final HmilyParticipant hmilyParticipant){
        boolean result = false;

        final HmilyInvocationContextParamSet hmilyInvocationContextParamSet =
                (HmilyInvocationContextParamSet)SingletonHolder.INST.get(ObjectProvide.class)
                        .provide(HmilyInvocationContextParamSet.class);
        if(null == hmilyInvocationContextParamSet){
            return false;
        }

        HmilyInvocationWithContext hmilyInvocationWithContext =
                (HmilyInvocationWithContext)hmilyParticipant.getConfirmHmilyInvocation();
        if(null == hmilyInvocationWithContext){
            hmilyInvocationWithContext = (HmilyInvocationWithContext)hmilyParticipant.getCancelHmilyInvocation();
        }

        Map<String, Object> contextParams = hmilyInvocationWithContext.getContextParams();

        result = hmilyInvocationContextParamSet.setContextParam(contextParams);

        return result;
    }

    /**
     * 清理上下文参数
     * 
     */
    private static void clearContextParams(Boolean flag){
        if(flag){
            final HmilyInvocationContextParamClear hmilyInvocationContextParamClear =
                    (HmilyInvocationContextParamClear)SingletonHolder.INST.get(ObjectProvide.class)
                            .provide(HmilyInvocationContextParamClear.class);
            if(hmilyInvocationContextParamClear != null) {
                hmilyInvocationContextParamClear.clearContextParam();
            }
        }
    }

    /**
     * Executor object.
     *
     * @param action           the action
     * @param executorType     the executor type
     * @param hmilyParticipant the hmily participant
     * @return the object
     * @throws Exception the exception
     */
    public static Object executor(final HmilyActionEnum action, final ExecutorTypeEnum executorType, final HmilyParticipant hmilyParticipant) throws Exception {
        Boolean isFillContextParam = false;
        try {
            setContext(action, hmilyParticipant);

            isFillContextParam = setContextParams(hmilyParticipant);

            if (executorType == ExecutorTypeEnum.RPC && hmilyParticipant.getRole() != HmilyRoleEnum.START.getCode()) {
//            setContext(action, hmilyParticipant);
                if (action == HmilyActionEnum.CONFIRMING) {
                    return executeRpc(hmilyParticipant.getConfirmHmilyInvocation());
                } else {
                    return executeRpc(hmilyParticipant.getCancelHmilyInvocation());
                }
            } else {
                if (action == HmilyActionEnum.CONFIRMING) {
                    return executeLocal(hmilyParticipant.getConfirmHmilyInvocation(), hmilyParticipant.getTargetClass(), hmilyParticipant.getConfirmMethod());
                } else {
                    return executeLocal(hmilyParticipant.getCancelHmilyInvocation(), hmilyParticipant.getTargetClass(), hmilyParticipant.getCancelMethod());
                }
            }
        }finally {
            clearContextParams(isFillContextParam);
        }
    }

    private static void setContext(final HmilyActionEnum action, final HmilyParticipant hmilyParticipant) {
        HmilyTransactionContext context = new HmilyTransactionContext();
        context.setAction(action.getCode());
        context.setTransId(hmilyParticipant.getTransId());
        context.setParticipantId(hmilyParticipant.getParticipantId());
        context.setRole(HmilyRoleEnum.START.getCode());
        context.setTransType(hmilyParticipant.getTransType());
        HmilyContextHolder.set(context);
    }

    private static Object executeLocal(final HmilyInvocation hmilyInvocation, final String className, final String methodName) throws Exception {
        if (Objects.isNull(hmilyInvocation)) {
            return null;
        }
        final Class<?> clazz = Class.forName(className);
        final Object[] args = hmilyInvocation.getArgs();
        final Class<?>[] parameterTypes = hmilyInvocation.getParameterTypes();
        final Object bean = SingletonHolder.INST.get(ObjectProvide.class).provide(clazz);
        return MethodUtils.invokeMethod(bean, methodName, args, parameterTypes);
    }

    private static Object executeRpc(final HmilyInvocation hmilyInvocation) throws Exception {
        if (Objects.isNull(hmilyInvocation)) {
            return null;
        }
        final Class<?> clazz = hmilyInvocation.getTargetClass();
        final String method = hmilyInvocation.getMethodName();
        final Object[] args = hmilyInvocation.getArgs();
        final Class<?>[] parameterTypes = hmilyInvocation.getParameterTypes();
        final Object bean = SingletonHolder.INST.get(ObjectProvide.class).provide(clazz);
        return MethodUtils.invokeMethod(bean, method, args, parameterTypes);
    }
}
