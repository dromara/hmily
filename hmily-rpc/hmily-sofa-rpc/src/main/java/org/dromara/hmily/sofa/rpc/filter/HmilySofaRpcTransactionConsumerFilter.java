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

package org.dromara.hmily.sofa.rpc.filter;

import com.alipay.sofa.rpc.common.utils.ClassTypeUtils;
import com.alipay.sofa.rpc.common.utils.ClassUtils;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.filter.AutoActive;
import com.alipay.sofa.rpc.filter.Filter;
import com.alipay.sofa.rpc.filter.FilterInvoker;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import lombok.SneakyThrows;
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
 * The type Hmily sofa rpc transaction filter.
 *
 * @author xiaoyu
 */
@Extension(value = "hmilySofaRpcTransactionConsumer")
@AutoActive(consumerSide = true)
public class HmilySofaRpcTransactionConsumerFilter extends Filter {
    
    @Override
    @SneakyThrows
    public SofaResponse invoke(final FilterInvoker invoker, final SofaRequest sofaRequest) throws SofaRpcException {
        final HmilyTransactionContext context = HmilyContextHolder.get();
        if (Objects.isNull(context)) {
            return invoker.invoke(sofaRequest);
        }
        Method method = sofaRequest.getMethod();
        Hmily hmily = method.getAnnotation(Hmily.class);
        if (Objects.isNull(hmily)) {
            return invoker.invoke(sofaRequest);
        }
        Long participantId = context.getParticipantId();
        final HmilyParticipant hmilyParticipant = buildParticipant(context, sofaRequest);
        Optional.ofNullable(hmilyParticipant).ifPresent(participant -> context.setParticipantId(participant.getParticipantId()));
        if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
            context.setParticipantRefId(participantId);
        }
        RpcMediator.getInstance().transmit(sofaRequest::addRequestProp, context);
        final SofaResponse result = invoker.invoke(sofaRequest);
        //if result has not exception
        if (!result.isError()) {
            if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
                HmilyTransactionHolder.getInstance().registerParticipantByNested(participantId, hmilyParticipant);
            } else {
                HmilyTransactionHolder.getInstance().registerStarterParticipant(hmilyParticipant);
            }
        } else {
            throw new HmilyRuntimeException("rpc invoke exception" + result.getErrorMsg());
        }
        return result;
    }
    
    private HmilyParticipant buildParticipant(final HmilyTransactionContext context, final SofaRequest sofaRequest) throws HmilyRuntimeException {
        if (HmilyActionEnum.TRYING.getCode() != context.getAction()) {
            return null;
        }
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setParticipantId(IdWorkerUtils.getInstance().createUUID());
        hmilyParticipant.setTransId(context.getTransId());
        hmilyParticipant.setTransType(context.getTransType());
        String methodName = sofaRequest.getMethodName();
        Class<?> clazz = ClassUtils.forName(sofaRequest.getInterfaceName());
        Class<?>[] args = ClassTypeUtils.getClasses(sofaRequest.getMethodArgSigs());
        final Object[] arguments = sofaRequest.getMethodArgs();
        HmilyInvocation hmilyInvocation = new HmilyInvocation(clazz, methodName, args, arguments);
        hmilyParticipant.setConfirmHmilyInvocation(hmilyInvocation);
        hmilyParticipant.setCancelHmilyInvocation(hmilyInvocation);
        return hmilyParticipant;
    }
}
