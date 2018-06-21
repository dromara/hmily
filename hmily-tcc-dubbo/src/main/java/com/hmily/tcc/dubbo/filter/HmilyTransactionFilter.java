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

package com.hmily.tcc.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.hmily.tcc.annotation.Tcc;
import com.hmily.tcc.common.bean.context.TccTransactionContext;
import com.hmily.tcc.common.bean.entity.Participant;
import com.hmily.tcc.common.bean.entity.TccInvocation;
import com.hmily.tcc.common.constant.CommonConstant;
import com.hmily.tcc.common.enums.TccActionEnum;
import com.hmily.tcc.common.enums.TccRoleEnum;
import com.hmily.tcc.common.exception.TccRuntimeException;
import com.hmily.tcc.common.utils.GsonUtils;
import com.hmily.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.hmily.tcc.core.service.executor.HmilyTransactionExecutor;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * impl dubbo filter.
 * @author xiaoyu
 */
@Activate(group = {Constants.SERVER_KEY, Constants.CONSUMER})
public class HmilyTransactionFilter implements Filter {

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
    @SuppressWarnings("unchecked")
    public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
        String methodName = invocation.getMethodName();
        Class clazz = invoker.getInterface();
        Class[] args = invocation.getParameterTypes();
        final Object[] arguments = invocation.getArguments();
        Method method = null;
        Tcc tcc = null;
        try {
            method = clazz.getDeclaredMethod(methodName, args);
            tcc = method.getAnnotation(Tcc.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (Objects.nonNull(tcc)) {
            try {
                final TccTransactionContext tccTransactionContext = TransactionContextLocal.getInstance().get();
                if (Objects.nonNull(tccTransactionContext)) {
                    RpcContext.getContext()
                            .setAttachment(CommonConstant.TCC_TRANSACTION_CONTEXT, GsonUtils.getInstance().toJson(tccTransactionContext));
                }
                final Result result = invoker.invoke(invocation);
                //如果result 没有异常就保存
                if (!result.hasException()) {
                    final Participant participant = buildParticipant(tccTransactionContext, tcc, method, clazz, arguments, args);
                    if (tccTransactionContext.getRole() == TccRoleEnum.PROVIDER.getCode()) {
                        hmilyTransactionExecutor.registerByNested(tccTransactionContext.getTransId(),
                                participant);
                    } else {
                        hmilyTransactionExecutor.enlistParticipant(participant);
                    }
                }
                return result;
            } catch (RpcException e) {
                e.printStackTrace();
                throw e;
            }
        } else {
            return invoker.invoke(invocation);
        }
    }

    @SuppressWarnings("unchecked")
    private Participant buildParticipant(final TccTransactionContext tccTransactionContext,
                                         final Tcc tcc,
                                         final Method method, final Class clazz,
                                         final Object[] arguments, final Class... args) throws TccRuntimeException {

        if (Objects.isNull(tccTransactionContext)
                || (TccActionEnum.TRYING.getCode() != tccTransactionContext.getAction())) {
            return null;
        }
        //获取协调方法
        String confirmMethodName = tcc.confirmMethod();
        if (StringUtils.isBlank(confirmMethodName)) {
            confirmMethodName = method.getName();
        }
        String cancelMethodName = tcc.cancelMethod();
        if (StringUtils.isBlank(cancelMethodName)) {
            cancelMethodName = method.getName();
        }
        TccInvocation confirmInvocation = new TccInvocation(clazz, confirmMethodName, args, arguments);
        TccInvocation cancelInvocation = new TccInvocation(clazz, cancelMethodName, args, arguments);
        //封装调用点
        return new Participant(tccTransactionContext.getTransId(), confirmInvocation, cancelInvocation);
    }
}
