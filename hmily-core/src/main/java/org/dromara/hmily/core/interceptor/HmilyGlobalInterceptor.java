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
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.mediator.LocalParameterLoader;
import org.dromara.hmily.core.mediator.RpcParameterLoader;
import org.dromara.hmily.core.service.HmilyTransactionHandlerFactory;
import org.dromara.hmily.spi.ExtensionLoaderFactory;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Optional;

/**
 * The type Hmily global interceptor.
 *
 * @author xiaoyu
 */
public class HmilyGlobalInterceptor implements HmilyTransactionInterceptor {
    
    private static RpcParameterLoader parameterLoader;
    
    private static final EnumMap<TransTypeEnum, HmilyTransactionHandlerFactory> FACTORY_MAP = new EnumMap<>(TransTypeEnum.class);
    
    static {
        parameterLoader = Optional.ofNullable(ExtensionLoaderFactory.load(RpcParameterLoader.class)).orElse(new LocalParameterLoader());
    }
    
    static {
        FACTORY_MAP.put(TransTypeEnum.TCC, ExtensionLoaderFactory.load(HmilyTransactionHandlerFactory.class, "tcc"));
        FACTORY_MAP.put(TransTypeEnum.TAC, ExtensionLoaderFactory.load(HmilyTransactionHandlerFactory.class, "tac"));
    }
    
    @Override
    public Object invoke(final ProceedingJoinPoint pjp) throws Throwable {
        HmilyTransactionContext context = parameterLoader.load();
        return invokeWithinTransaction(context, pjp);
    }
    
    private Object invokeWithinTransaction(final HmilyTransactionContext hmilyTransactionContext, final ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        final HmilyTCC hmilyTCC = method.getAnnotation(HmilyTCC.class);
        if (null != hmilyTCC) {
            return FACTORY_MAP.get(TransTypeEnum.TCC).factoryOf(hmilyTransactionContext).handler(point, hmilyTransactionContext);
        } else {
            return FACTORY_MAP.get(TransTypeEnum.TAC).factoryOf(hmilyTransactionContext).handler(point, hmilyTransactionContext);
        }
    }
}
