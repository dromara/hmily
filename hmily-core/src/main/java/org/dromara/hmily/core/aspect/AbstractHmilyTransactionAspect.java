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

package org.dromara.hmily.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.core.interceptor.HmilyGlobalInterceptor;
import org.dromara.hmily.core.interceptor.HmilyTransactionInterceptor;

/**
 * this is aspect handler.
 *
 * @author xiaoyu
 */
@Aspect
public abstract class AbstractHmilyTransactionAspect {

    private final HmilyTransactionInterceptor interceptor = new HmilyGlobalInterceptor();

    /**
     * this is point cut with {@linkplain HmilyTCC }.
     */
    @Pointcut("@annotation(org.dromara.hmily.annotation.HmilyTCC) || @annotation(org.dromara.hmily.annotation.HmilyTAC) ||  @annotation(org.dromara.hmily.annotation.HmilyXA)")
    public void hmilyInterceptor() {
    }

    /**
     * this is around in {@linkplain HmilyTCC }.
     *
     * @param proceedingJoinPoint proceedingJoinPoint
     * @return Object object
     * @throws Throwable Throwable
     */
    @Around("hmilyInterceptor()")
    public Object interceptTccMethod(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return interceptor.invoke(proceedingJoinPoint);
    }
}
