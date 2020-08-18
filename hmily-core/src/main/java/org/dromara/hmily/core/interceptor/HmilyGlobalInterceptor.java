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

import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.mediator.LocalParameterLoader;
import org.dromara.hmily.core.mediator.RpcParameterLoader;
import org.dromara.hmily.core.service.HmilyTransactionAspectInvoker;
import org.dromara.hmily.spi.ExtensionLoaderFactory;

/**
 * The type Hmily global interceptor.
 *
 * @author xiaoyu
 */
public class HmilyGlobalInterceptor implements HmilyTransactionInterceptor {
    
    private static RpcParameterLoader parameterLoader;
    
    static {
        parameterLoader = Optional.ofNullable(ExtensionLoaderFactory.load(RpcParameterLoader.class)).orElse(new LocalParameterLoader());
    }
    
    @Override
    public Object interceptor(final ProceedingJoinPoint pjp) throws Throwable {
        HmilyTransactionContext context = parameterLoader.load();
        return HmilyTransactionAspectInvoker.getInstance().invoke(context, pjp);
    }
}
