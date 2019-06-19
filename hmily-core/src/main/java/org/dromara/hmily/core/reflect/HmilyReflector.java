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

import org.apache.commons.lang3.reflect.MethodUtils;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.bean.entity.HmilyInvocation;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.dromara.hmily.core.helper.SpringBeanUtils;

import java.util.Objects;

/**
 * The type Hmily reflector.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyReflector {

    /**
     * Executor object.
     *
     * @param transId         the trans id
     * @param actionEnum      the action enum
     * @param hmilyInvocation the hmily invocation
     * @return the object
     * @throws Exception the exception
     */
    public static Object executor(final String transId, final HmilyActionEnum actionEnum,
                                  final HmilyInvocation hmilyInvocation) throws Exception {
        HmilyTransactionContext context = new HmilyTransactionContext();
        context.setAction(actionEnum.getCode());
        context.setTransId(transId);
        context.setRole(HmilyRoleEnum.START.getCode());
        HmilyTransactionContextLocal.getInstance().set(context);
        return execute(hmilyInvocation);
    }

    @SuppressWarnings("unchecked")
    private static Object execute(final HmilyInvocation hmilyInvocation) throws Exception {
        if (Objects.isNull(hmilyInvocation)) {
            return null;
        }
        final Class clazz = hmilyInvocation.getTargetClass();
        final String method = hmilyInvocation.getMethodName();
        final Object[] args = hmilyInvocation.getArgs();
        final Class[] parameterTypes = hmilyInvocation.getParameterTypes();
        final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
        return MethodUtils.invokeMethod(bean, method, args, parameterTypes);

    }
}
