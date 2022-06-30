/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dromara.hmily.xa.rpc.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.service.HmilyTransactionHandler;

/**
 * DefHmilyTransactionHandler .
 * 如果为null的时候，需要执行相关的业务逻辑。.
 *
 * default
 * 处理context为null的情况
 *
 * @author sixh chenbin
 */
public class DefHmilyTransactionHandler implements HmilyTransactionHandler {

    @Override
    public Object handleTransaction(final ProceedingJoinPoint point, final HmilyTransactionContext hmilyTransactionContext) throws Throwable {
        return point.proceed();
    }
}
