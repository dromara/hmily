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

package org.dromara.hmily.motan.filter;

import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.core.extension.Activation;
import com.weibo.api.motan.core.extension.SpiMeta;
import com.weibo.api.motan.filter.Filter;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;
import com.weibo.api.motan.util.ReflectUtil;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.core.service.executor.HmilyTransactionExecutor;
import org.dromara.hmily.repository.spi.entity.HmilyInvocation;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;

/**
 * The MotanHmilyTransactionFilter.
 *
 * @author xiaoyu
 */
@SpiMeta(name = "motanHmilyTransactionFilter")
@Activation(key = {MotanConstants.NODE_TYPE_REFERER})
public class MotanHmilyTransactionFilter implements Filter {

    @Override
    public Response filter(final Caller<?> caller, final Request request) {
        final HmilyTransactionContext context = HmilyContextHolder.get();
        if (Objects.isNull(context) || HmilyActionEnum.TRYING.getCode() != context.getAction()) {
            return caller.call(request);
        }
        RpcMediator.getInstance().transmit(request::setAttachment, context);
    
        final Response response = caller.call(request);
        if (null != response.getException()) {
            final HmilyParticipant hmilyParticipant = buildParticipant(context, request);
            if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
                HmilyTransactionExecutor.getInstance().registerParticipantByNested(context.getParticipantId(), hmilyParticipant);
            } else {
                HmilyTransactionExecutor.getInstance().enlistParticipant(hmilyParticipant);
            }
        }
        return response;
    }
    
    @SneakyThrows
    private HmilyParticipant buildParticipant(final HmilyTransactionContext context, final Request request) throws HmilyRuntimeException {
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setTransId(context.getTransId());
        hmilyParticipant.setTransType(context.getTransType());
        final String interfaceName = request.getInterfaceName();
        final String methodName = request.getMethodName();
        final Object[] arguments = request.getArguments();
        Class<?> clazz = ReflectUtil.forName(interfaceName);
        final Method[] methods = clazz.getMethods();
        Class<?>[] args = Stream.of(methods)
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .map(Method::getParameterTypes).get();
        HmilyInvocation hmilyInvocation = new HmilyInvocation(clazz, methodName, args, arguments);
        hmilyParticipant.setConfirmHmilyInvocation(hmilyInvocation);
        hmilyParticipant.setCancelHmilyInvocation(hmilyInvocation);
        return hmilyParticipant;
    }
}
