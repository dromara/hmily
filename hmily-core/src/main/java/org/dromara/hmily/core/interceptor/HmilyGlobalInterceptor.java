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

package org.dromara.hmily.core.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.hmily.annotation.HmilyTAC;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.annotation.HmilyXA;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.mediator.LocalParameterLoader;
import org.dromara.hmily.core.mediator.RpcParameterLoader;
import org.dromara.hmily.core.service.HmilyTransactionHandlerRegistry;
import org.dromara.hmily.spi.ExtensionLoaderFactory;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Optional;

/**
 * The type Hmily global interceptor.
 *
 * @author xiaoyu
 * @author zhaojun
 */
public class HmilyGlobalInterceptor implements HmilyTransactionInterceptor {

    private static RpcParameterLoader parameterLoader;

    private static final EnumMap<TransTypeEnum, HmilyTransactionHandlerRegistry> REGISTRY = new EnumMap<>(TransTypeEnum.class);

    static {
        parameterLoader = Optional.ofNullable(ExtensionLoaderFactory.load(RpcParameterLoader.class)).orElse(new LocalParameterLoader());
    }

    static {
        REGISTRY.put(TransTypeEnum.TCC, ExtensionLoaderFactory.load(HmilyTransactionHandlerRegistry.class, "tcc"));
        REGISTRY.put(TransTypeEnum.TAC, ExtensionLoaderFactory.load(HmilyTransactionHandlerRegistry.class, "tac"));
        REGISTRY.put(TransTypeEnum.XA, ExtensionLoaderFactory.load(HmilyTransactionHandlerRegistry.class, "xa"));
    }

    @Override
    public Object invoke(final ProceedingJoinPoint pjp) throws Throwable {
        HmilyTransactionContext context = parameterLoader.load();
        return invokeWithinTransaction(context, pjp);
    }

    private Object invokeWithinTransaction(final HmilyTransactionContext context, final ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        return getRegistry(signature.getMethod()).select(context).handleTransaction(point, context);
    }

    private HmilyTransactionHandlerRegistry getRegistry(final Method method) {
        if (method.isAnnotationPresent(HmilyTCC.class)) {
            return REGISTRY.get(TransTypeEnum.TCC);
        } else if (method.isAnnotationPresent(HmilyTAC.class)) {
            return REGISTRY.get(TransTypeEnum.TAC);
        } else if (method.isAnnotationPresent(HmilyXA.class)) {
            return REGISTRY.get(TransTypeEnum.XA);
        } else {
            return REGISTRY.get(TransTypeEnum.TAC);
        }
        //  return null != method.getAnnotation(HmilyTCC.class) ? REGISTRY.get(TransTypeEnum.TCC) : REGISTRY.get(TransTypeEnum.TAC);
    }
}
