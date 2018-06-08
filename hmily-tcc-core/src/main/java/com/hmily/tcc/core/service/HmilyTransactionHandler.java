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

package com.hmily.tcc.core.service;

import com.hmily.tcc.common.bean.context.TccTransactionContext;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * TccTransactionHandler.
 * @author xiaoyu
 */
@FunctionalInterface
public interface HmilyTransactionHandler {

    /**
     * aop handler.
     *
     * @param point                 point
     * @param tccTransactionContext transaction context
     * @return Object
     * @throws Throwable e
     */
    Object handler(ProceedingJoinPoint point, TccTransactionContext tccTransactionContext) throws Throwable;
}
