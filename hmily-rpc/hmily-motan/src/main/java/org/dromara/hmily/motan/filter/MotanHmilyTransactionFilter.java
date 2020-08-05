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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.dromara.hmily.annotation.HmilyTAC;
import org.dromara.hmily.annotation.HmilyTCC;
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
        if (Objects.isNull(context)) {
            return caller.call(request);
        }
        final String interfaceName = request.getInterfaceName();
        final String methodName = request.getMethodName();
        try {
            //他妈的 这里还要拿方法参数类型
            Class<?> clazz = ReflectUtil.forName(interfaceName);
            final Method[] methods = clazz.getMethods();
            Class<?>[] args = Stream.of(methods)
                    .filter(m -> m.getName().equals(methodName))
                    .findFirst()
                    .map(Method::getParameterTypes).get();
            Method method = clazz.getMethod(methodName, args);
            Annotation[] annotations = method.getAnnotations();
            boolean match = Arrays.stream(annotations)
                    .anyMatch(annotation -> annotation.annotationType().equals(HmilyTCC.class)
                            || annotation.annotationType().equals(HmilyTAC.class));
            if (!match) {
                return caller.call(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Long participantId = context.getParticipantId();
        final HmilyParticipant hmilyParticipant = buildParticipant(context, request);
        Optional.ofNullable(hmilyParticipant).ifPresent(h -> context.setParticipantId(h.getParticipantId()));
        if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
            context.setParticipantRefId(participantId);
        }
        RpcMediator.getInstance().transmit(request::setAttachment, context);
        final Response response = caller.call(request);
        if (null != response.getException()) {
            if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
                HmilyTransactionHolder.getInstance().registerParticipantByNested(context.getParticipantId(), hmilyParticipant);
            } else {
                HmilyTransactionHolder.getInstance().registerStarterParticipant(hmilyParticipant);
            }
        }
        return response;
    }
    
    @SneakyThrows
    private HmilyParticipant buildParticipant(final HmilyTransactionContext context, final Request request) throws HmilyRuntimeException {
        if (HmilyActionEnum.TRYING.getCode() != context.getAction()) {
            return null;
        }
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setTransId(context.getTransId());
        hmilyParticipant.setTransType(context.getTransType());
        hmilyParticipant.setParticipantId(IdWorkerUtils.getInstance().createUUID());
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
