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

package org.dromara.hmily.dubbo.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.bean.entity.HmilyInvocation;
import org.dromara.hmily.common.bean.entity.HmilyParticipant;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.core.service.executor.HmilyTransactionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * impl dubbo filter.
 *
 * @author xiaoyu
 */
@Activate(group = {Constants.SERVER_KEY, Constants.CONSUMER})
@SuppressWarnings("all")
public class DubboHmilyTransactionFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboHmilyTransactionFilter.class);

    private HmilyTransactionExecutor hmilyTransactionExecutor;

    /**
     * this is init by dubbo spi
     * set hmilyTransactionExecutor.
     *
     * @param hmilyTransactionExecutor {@linkplain HmilyTransactionExecutor }
     */
    public void setHmilyTransactionExecutor(final HmilyTransactionExecutor hmilyTransactionExecutor) {
        this.hmilyTransactionExecutor = hmilyTransactionExecutor;
    }

    @Override
    public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
        String methodName = invocation.getMethodName();
        Class clazz = invoker.getInterface();
        Class[] args = invocation.getParameterTypes();
        final Object[] arguments = invocation.getArguments();
        Method method = null;
        Hmily hmily = null;
        try {
            converterParamsClass(args, arguments);
            method = clazz.getMethod(methodName, args);
            hmily = method.getAnnotation(Hmily.class);
        } catch (Exception ex) {
            LOGGER.error("hmily find method error {} ", ex);
        }
        if (Objects.nonNull(hmily)) {
            try {
                final HmilyTransactionContext hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();
                if (Objects.nonNull(hmilyTransactionContext)) {
                    RpcMediator.getInstance().transmit(RpcContext.getContext()::setAttachment, hmilyTransactionContext);
                    final Result result = invoker.invoke(invocation);
                    //if result has not exception
                    if (!result.hasException()) {
                        final HmilyParticipant hmilyParticipant = buildParticipant(hmilyTransactionContext, hmily, method, clazz, arguments, args);
                        if (hmilyTransactionContext.getRole() == HmilyRoleEnum.INLINE.getCode()) {
                            hmilyTransactionExecutor.registerByNested(hmilyTransactionContext.getTransId(),
                                    hmilyParticipant);
                        } else {
                            hmilyTransactionExecutor.enlistParticipant(hmilyParticipant);
                        }
                    } else {
                        throw new HmilyRuntimeException("rpc invoke exception{}", result.getException());
                    }
                    return result;
                }
                return invoker.invoke(invocation);
            } catch (RpcException e) {
                e.printStackTrace();
                throw e;
            }
        } else {
            return invoker.invoke(invocation);
        }
    }

    @SuppressWarnings("unchecked")
    private HmilyParticipant buildParticipant(final HmilyTransactionContext hmilyTransactionContext,
                                              final Hmily hmily,
                                              final Method method, final Class clazz,
                                              final Object[] arguments, final Class... args) throws HmilyRuntimeException {

        if (Objects.isNull(hmilyTransactionContext)
                || (HmilyActionEnum.TRYING.getCode() != hmilyTransactionContext.getAction())) {
            return null;
        }
        //获取协调方法
        String confirmMethodName = hmily.confirmMethod();
        if (StringUtils.isBlank(confirmMethodName)) {
            confirmMethodName = method.getName();
        }
        String cancelMethodName = hmily.cancelMethod();
        if (StringUtils.isBlank(cancelMethodName)) {
            cancelMethodName = method.getName();
        }
        HmilyInvocation confirmInvocation = new HmilyInvocation(clazz, confirmMethodName, args, arguments);
        HmilyInvocation cancelInvocation = new HmilyInvocation(clazz, cancelMethodName, args, arguments);
        //封装调用点
        return new HmilyParticipant(hmilyTransactionContext.getTransId(), confirmInvocation, cancelInvocation);
    }

    private void converterParamsClass(final Class[] args, final Object[] arguments) {
        if (arguments == null || arguments.length < 1) {
            return;
        }
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] != null) {
                args[i] = arguments[i].getClass();
            }
        }
    }
}
