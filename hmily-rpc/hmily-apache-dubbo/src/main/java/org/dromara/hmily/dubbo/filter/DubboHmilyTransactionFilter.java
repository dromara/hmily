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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.dromara.hmily.annotation.HmilyTAC;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.utils.IdWorkerUtils;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.holder.HmilyTransactionHolder;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.repository.spi.entity.HmilyInvocation;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * impl dubbo filter.
 *
 * @author xiaoyu
 */
@Activate(group = Constants.CONSUMER)
public class DubboHmilyTransactionFilter implements Filter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboHmilyTransactionFilter.class);
    
    @Override
    public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
        final HmilyTransactionContext context = HmilyContextHolder.get();
        if (Objects.isNull(context)) {
            return invoker.invoke(invocation);
        }
        Class<?> clazz = invoker.getInterface();
        Class<?>[] args = invocation.getParameterTypes();
        final Object[] arguments = invocation.getArguments();
        String methodName = invocation.getMethodName();
        try {
            converterParamsClass(args, arguments);
            Method method = clazz.getMethod(methodName, args);
            Annotation[] annotations = method.getAnnotations();
            boolean match = Arrays.stream(annotations)
                    .anyMatch(annotation -> annotation.annotationType().equals(HmilyTCC.class)
                            || annotation.annotationType().equals(HmilyTAC.class));
            if (!match) {
                return invoker.invoke(invocation);
            }
        } catch (Exception ex) {
            LogUtil.error(LOGGER, "hmily find method error {} ", ex::getMessage);
        }
        Long participantId = context.getParticipantId();
        final HmilyParticipant hmilyParticipant = buildParticipant(context, invoker, invocation);
        Optional.ofNullable(hmilyParticipant).ifPresent(h -> context.setParticipantId(h.getParticipantId()));
        if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
            context.setParticipantRefId(participantId);
        }
        RpcMediator.getInstance().transmit(RpcContext.getContext()::setAttachment, context);
        final Result result = invoker.invoke(invocation);
        //if result has not exception
        if (!result.hasException()) {
            if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
                HmilyTransactionHolder.getInstance().registerParticipantByNested(participantId, hmilyParticipant);
            } else {
                HmilyTransactionHolder.getInstance().registerStarterParticipant(hmilyParticipant);
            }
        } else {
            throw new HmilyRuntimeException("rpc invoke exception{}", result.getException());
        }
        return result;
    }
    
    private HmilyParticipant buildParticipant(final HmilyTransactionContext context,
                                              final Invoker<?> invoker, final Invocation invocation) throws HmilyRuntimeException {
        if (HmilyActionEnum.TRYING.getCode() != context.getAction()) {
            return null;
        }
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setTransId(context.getTransId());
        hmilyParticipant.setParticipantId(IdWorkerUtils.getInstance().createUUID());
        hmilyParticipant.setTransType(context.getTransType());
        String methodName = invocation.getMethodName();
        Class<?> clazz = invoker.getInterface();
        Class<?>[] args = invocation.getParameterTypes();
        final Object[] arguments = invocation.getArguments();
        HmilyInvocation hmilyInvocation = new HmilyInvocation(clazz, methodName, args, arguments);
        hmilyParticipant.setConfirmHmilyInvocation(hmilyInvocation);
        hmilyParticipant.setCancelHmilyInvocation(hmilyInvocation);
        return hmilyParticipant;
    }
    
    private void converterParamsClass(final Class<?>[] args, final Object[] arguments) {
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
