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

package org.dromara.hmily.springcloud.feign;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.utils.IdWorkerUtils;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.holder.HmilyTransactionHolder;
import org.dromara.hmily.repository.spi.entity.HmilyInvocation;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HmilyFeignHandler.
 *
 * @author xiaoyu
 */
public class HmilyFeignHandler implements InvocationHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyFeignHandler.class);
    
    private InvocationHandler delegate;
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        } else {
            final HmilyTransactionContext context = HmilyContextHolder.get();
            if (Objects.isNull(context)) {
                return this.delegate.invoke(proxy, method, args);
            }
            final HmilyTCC hmily = method.getAnnotation(HmilyTCC.class);
            if (Objects.isNull(hmily)) {
                return this.delegate.invoke(proxy, method, args);
            }
            try {
                Long participantId = context.getParticipantId();
                final HmilyParticipant hmilyParticipant = buildParticipant(method, args, context);
                Optional.ofNullable(hmilyParticipant).ifPresent(participant -> context.setParticipantId(participant.getParticipantId()));
                if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
                    context.setParticipantRefId(participantId);
                }
                final Object invoke = delegate.invoke(proxy, method, args);
                if (context.getRole() == HmilyRoleEnum.PARTICIPANT.getCode()) {
                    HmilyTransactionHolder.getInstance().registerParticipantByNested(context.getParticipantId(), hmilyParticipant);
                } else {
                    HmilyTransactionHolder.getInstance().registerStarterParticipant(hmilyParticipant);
                }
                return invoke;
            } catch (Throwable e) {
                LOGGER.error("HmilyFeignHandler invoker exception :", e);
                throw e;
            }
        }
    }
    
    private HmilyParticipant buildParticipant(final Method method, final Object[] args, final HmilyTransactionContext context) {
        if (HmilyActionEnum.TRYING.getCode() != context.getAction()) {
            return null;
        }
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setParticipantId(IdWorkerUtils.getInstance().createUUID());
        hmilyParticipant.setTransId(context.getTransId());
        hmilyParticipant.setTransType(context.getTransType());
        final Class<?> declaringClass = method.getDeclaringClass();
        HmilyInvocation hmilyInvocation = new HmilyInvocation(declaringClass, method.getName(), method.getParameterTypes(), args);
        hmilyParticipant.setConfirmHmilyInvocation(hmilyInvocation);
        hmilyParticipant.setCancelHmilyInvocation(hmilyInvocation);
        return hmilyParticipant;
    }
    
    void setDelegate(final InvocationHandler delegate) {
        this.delegate = delegate;
    }
}
