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

package org.dromara.hmily.grpc.filter;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import io.grpc.Channel;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.ForwardingClientCallListener;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.utils.IdWorkerUtils;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.holder.HmilyTransactionHolder;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.grpc.parameter.GrpcHmilyContext;
import org.dromara.hmily.repository.spi.entity.HmilyInvocation;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * grpc client filter.
 *
 * @author tydhot
 */
public class GrpcHmilyTransactionFilter implements ClientInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcHmilyTransactionFilter.class);

    @Override
    public <R, P> ClientCall<R, P> interceptCall(final MethodDescriptor<R, P> methodDescriptor,
                                                           final CallOptions callOptions, final Channel channel) {
        final HmilyTransactionContext context = HmilyContextHolder.get();
        if (Objects.isNull(context) || Objects.isNull(GrpcHmilyContext.getHmilyClass().get())
            || Objects.isNull(GrpcHmilyContext.getHmilyParam().get())) {
            return channel.newCall(methodDescriptor, callOptions);
        }
        String[] clazzNameAndMethod = methodDescriptor.getFullMethodName().split("/");
        try {
            Class<?> clazz = GrpcHmilyContext.getHmilyClass().get().getClass();
            String methodName = clazzNameAndMethod[1];
            Method method = null;
            for (Method tempMethod : clazz.getMethods()) {
                if (tempMethod.getName().equals(methodName)) {
                    method = tempMethod;
                    break;
                }
            }
            if (method != null) {
                Class<?>[] arg = method.getParameterTypes();
                Long participantId = context.getParticipantId();
                HmilyParticipant hmilyParticipant = buildParticipant(context, methodName, clazz, arg);
                Optional.ofNullable(hmilyParticipant)
                    .ifPresent(participant -> context.setParticipantId(participant.getParticipantId()));
                if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
                    context.setParticipantRefId(participantId);
                }
                return new ForwardingClientCall.SimpleForwardingClientCall<R, P>(channel.newCall(methodDescriptor, callOptions)) {
                    @Override
                    public void start(final Listener<P> responseListener, final Metadata headers) {
                        RpcMediator.getInstance().transmit((key, value) -> headers.put(GrpcHmilyContext.HMILY_META_DATA, value), context);
                        super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<P>(responseListener) {
                            public void onClose(final Status status, final Metadata trailers) {
                                if (status.getCode().value() == Status.Code.OK.value()) {
                                    if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
                                        HmilyTransactionHolder.getInstance().registerParticipantByNested(participantId, hmilyParticipant);
                                    } else {
                                        HmilyTransactionHolder.getInstance().registerStarterParticipant(hmilyParticipant);
                                    }
                                } else {
                                    onInvokeFail(status);
                                }
                                GrpcHmilyContext.removeAfterInvoke();
                                super.onClose(status, trailers);
                            } }, headers);
                    }
                };
            }
        } catch (Exception e) {
            LOGGER.error("exception is {}", e.getMessage());
        }
        return channel.newCall(methodDescriptor, callOptions);
    }

    private HmilyParticipant buildParticipant(final HmilyTransactionContext context, final String methodName,
        final Class<?> clazz, final Class<?>[] args) throws HmilyRuntimeException {
        if (HmilyActionEnum.TRYING.getCode() != context.getAction()) {
            return null;
        }
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setParticipantId(IdWorkerUtils.getInstance().createUUID());
        hmilyParticipant.setTransId(context.getTransId());
        hmilyParticipant.setTransType(context.getTransType());
        final Object[] arguments = new Object[] {GrpcHmilyContext.getHmilyParam().get()};
        HmilyInvocation hmilyInvocation = new HmilyInvocation(clazz, methodName, args, arguments);
        hmilyParticipant.setConfirmHmilyInvocation(hmilyInvocation);
        hmilyParticipant.setCancelHmilyInvocation(hmilyInvocation);
        return hmilyParticipant;
    }

    private void onInvokeFail(final Status status) {
        GrpcHmilyContext.removeAfterInvoke();
        throw new HmilyRuntimeException("rpc invoke exception{}", status.getCause());
    }
}
