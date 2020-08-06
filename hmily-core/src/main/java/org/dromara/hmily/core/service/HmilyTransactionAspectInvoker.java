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

package org.dromara.hmily.core.service;

import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.spi.ExtensionLoaderFactory;

/**
 * HmilyTransactionAspectServiceImpl.
 *
 * @author xiaoyu
 */
public final class HmilyTransactionAspectInvoker {
    
    private static final HmilyTransactionAspectInvoker INSTANCE = new HmilyTransactionAspectInvoker();
    
    private HmilyTransactionAspectInvoker() {
    }
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static HmilyTransactionAspectInvoker getInstance() {
        return INSTANCE;
    }
    
    /**
     * hmily transaction aspect.
     *
     * @param hmilyTransactionContext {@linkplain  HmilyTransactionContext}
     * @param point                   {@linkplain ProceedingJoinPoint}
     * @return object return value
     * @throws Throwable exception
     */
    public Object invoke(final HmilyTransactionContext hmilyTransactionContext, final ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        final HmilyTCC hmilyTCC = method.getAnnotation(HmilyTCC.class);
        if (null != hmilyTCC) {
            return ExtensionLoaderFactory.load(HmilyTransactionHandlerFactory.class, "tcc").factoryOf(hmilyTransactionContext).handler(point, hmilyTransactionContext);
        } else {
            return ExtensionLoaderFactory.load(HmilyTransactionHandlerFactory.class, "tac").factoryOf(hmilyTransactionContext).handler(point, hmilyTransactionContext);
        }
    }
}
