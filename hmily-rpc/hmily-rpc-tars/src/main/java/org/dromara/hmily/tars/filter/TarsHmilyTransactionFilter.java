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

package org.dromara.hmily.tars.filter;

import com.qq.tars.common.Filter;
import com.qq.tars.common.FilterChain;
import com.qq.tars.net.core.Request;
import com.qq.tars.net.core.Response;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.rpc.exc.ServerException;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.server.core.ContextManager;
import org.dromara.hmily.annotation.Hmily;
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

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * impl tars filter.
 *
 * @author tydhot
 */
public class TarsHmilyTransactionFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TarsHmilyTransactionFilter.class);

    @Override
    public void init() {

    }

    @Override
    public void doFilter(final Request request, final Response response, final FilterChain chain) throws Throwable {
        if (request instanceof TarsServantRequest && response instanceof TarsServantResponse) {
            TarsServantRequest tarsRequest = (TarsServantRequest) request;
            final HmilyTransactionContext context = HmilyContextHolder.get();
            if (Objects.isNull(context)) {
                chain.doFilter(request, response);
                return;
            }
            try {
                Method method = tarsRequest.getMethodInfo().getMethod();
                Hmily hmily = method.getAnnotation(Hmily.class);
                if (Objects.isNull(hmily)) {
                    chain.doFilter(request, response);
                    return;
                }
            } catch (Exception ex) {
                LogUtil.error(LOGGER, "hmily find method error {} ", ex::getMessage);
                chain.doFilter(request, response);
                return;
            }
            final Object[] arguments = tarsRequest.getMethodParameters();
            Class<?>[] args = tarsRequest.getMethodInfo().getMethod().getParameterTypes();
            converterParamsClass(args, arguments);
            Long participantId = context.getParticipantId();
            final HmilyParticipant hmilyParticipant = buildParticipant(context, tarsRequest);
            Optional.ofNullable(hmilyParticipant).ifPresent(participant -> context.setParticipantId(participant.getParticipantId()));
            if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
                context.setParticipantRefId(participantId);
            }
            RpcMediator.getInstance().transmit(ContextManager.getContext()::setAttribute, context);

            chain.doFilter(request, response);

            //if result has not exception
            TarsServantResponse tarsResponse = (TarsServantResponse) response;
            if (tarsResponse.getRet() != TarsHelper.SERVERSUCCESS) {
                if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
                    HmilyTransactionHolder.getInstance().registerParticipantByNested(participantId, hmilyParticipant);
                } else {
                    HmilyTransactionHolder.getInstance().registerStarterParticipant(hmilyParticipant);
                }
            } else {
                throw new HmilyRuntimeException("rpc invoke exception{}", ServerException.makeException(tarsResponse.getRet(), tarsResponse.getRemark()));
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private HmilyParticipant buildParticipant(final HmilyTransactionContext context,
                                              final TarsServantRequest request) throws HmilyRuntimeException {
        if (HmilyActionEnum.TRYING.getCode() != context.getAction()) {
            return null;
        }
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setTransId(context.getTransId());
        hmilyParticipant.setParticipantId(IdWorkerUtils.getInstance().createUUID());
        hmilyParticipant.setTransType(context.getTransType());
        String methodName = request.getMethodInfo().getMethodName();
        Class<?> clazz = request.getMethodInfo().getMethod().getDeclaringClass();
        Class<?>[] args = request.getMethodInfo().getMethod().getParameterTypes();
        final Object[] arguments = request.getMethodParameters();
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

    @Override
    public void destroy() {

    }
}
