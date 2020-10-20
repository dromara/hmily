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

package org.dromara.hmily.brpc.interceptor;

import com.baidu.brpc.RpcContext;
import com.baidu.brpc.exceptions.RpcException;
import com.baidu.brpc.interceptor.AbstractInterceptor;
import com.baidu.brpc.interceptor.InterceptorChain;
import com.baidu.brpc.protocol.Request;
import com.baidu.brpc.protocol.Response;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.utils.IdWorkerUtils;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.holder.HmilyTransactionHolder;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.repository.spi.entity.HmilyInvocation;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;

/**
 * The hmily brpc transaction interceptor.
 *
 * @author liu·yu
 */
public class BrpcHmilyTransactionInterceptor extends AbstractInterceptor {
    
    @Override
    public void aroundProcess(final Request request, final Response response, final InterceptorChain chain) throws RpcException {
        HmilyTransactionContext context = HmilyContextHolder.get();
        if (Objects.isNull(context)) {
            chain.intercept(request, response);
            return;
        }
        Method method = request.getRpcMethodInfo().getMethod();
        try {
            Hmily hmily = method.getAnnotation(Hmily.class);
            if (Objects.isNull(hmily)) {
                chain.intercept(request, response);
                return;
            }
        } catch (Exception ex) {
            chain.intercept(request, response);
            return;
        }
        Long participantId = context.getParticipantId();
        HmilyParticipant hmilyParticipant = buildParticipant(context, request);
        Optional.ofNullable(hmilyParticipant).ifPresent(participant -> context.setParticipantId(participant.getParticipantId()));
        if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
            context.setParticipantRefId(participantId);
        }
        RpcMediator.getInstance().transmit(RpcContext.getContext()::setRequestKvAttachment, context);
        if (request.getKvAttachment() == null) {
            request.setKvAttachment(RpcContext.getContext().getRequestKvAttachment());
        } else {
            request.getKvAttachment().putAll(RpcContext.getContext().getRequestKvAttachment());
        }
        try {
            chain.intercept(request, response);
            if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
                HmilyTransactionHolder.getInstance().registerParticipantByNested(participantId, hmilyParticipant);
            } else {
                HmilyTransactionHolder.getInstance().registerStarterParticipant(hmilyParticipant);
            }
        } catch (Exception e) {
            throw new HmilyRuntimeException("rpc invoke exception{}", e);
        }
    }

    private HmilyParticipant buildParticipant(final HmilyTransactionContext context, final Request request) {
        if (HmilyActionEnum.TRYING.getCode() != context.getAction()) {
            return null;
        }
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setParticipantId(IdWorkerUtils.getInstance().createUUID());
        hmilyParticipant.setTransId(context.getTransId());
        hmilyParticipant.setTransType(context.getTransType());
        Class<?> clazz = request.getRpcMethodInfo().getMethod().getDeclaringClass();
        String methodName = request.getRpcMethodInfo().getMethodName();
        Class<?>[] converter = converterParamsClass(request.getRpcMethodInfo().getInputClasses());
        Object[] args = request.getArgs();
        HmilyInvocation invocation = new HmilyInvocation(clazz, methodName, converter, args);
        hmilyParticipant.setConfirmHmilyInvocation(invocation);
        hmilyParticipant.setCancelHmilyInvocation(invocation);
        return hmilyParticipant;
    }

    private Class<?>[] converterParamsClass(final Type[] types) {
        Class<?>[] classes = new Class[types.length];
        for (int i = 0; i < types.length; i++) {
            classes[i] = (Class<?>) types[i];
        }
        return classes;
    }
}
