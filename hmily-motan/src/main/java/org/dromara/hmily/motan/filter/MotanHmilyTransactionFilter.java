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
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.annotation.PatternEnum;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.bean.entity.HmilyInvocation;
import org.dromara.hmily.common.bean.entity.HmilyParticipant;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.dromara.hmily.core.helper.SpringBeanUtils;
import org.dromara.hmily.core.service.executor.HmilyTransactionExecutor;
import org.dromara.hmily.core.mediator.RpcMediator;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The MotanHmilyTransactionFilter.
 *
 * @author xiaoyu
 */
@SpiMeta(name = "motanHmilyTransactionFilter")
@Activation(key = {MotanConstants.NODE_TYPE_REFERER})
public class MotanHmilyTransactionFilter implements Filter {

    @Override
    @SuppressWarnings("unchecked")
    public Response filter(final Caller<?> caller, final Request request) {
        final String interfaceName = request.getInterfaceName();
        final String methodName = request.getMethodName();
        final Object[] arguments = request.getArguments();
        Class[] args = null;
        Method method = null;
        Hmily hmily = null;
        Class clazz = null;
        try {
            //他妈的 这里还要拿方法参数类型
            clazz = ReflectUtil.forName(interfaceName);
            final Method[] methods = clazz.getMethods();
            args = Stream.of(methods)
                    .filter(m -> m.getName().equals(methodName))
                    .findFirst()
                    .map(Method::getParameterTypes).get();
            method = clazz.getMethod(methodName, args);
            hmily = method.getAnnotation(Hmily.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Objects.nonNull(hmily)) {
            try {
                final HmilyTransactionExecutor hmilyTransactionExecutor = SpringBeanUtils.getInstance().getBean(HmilyTransactionExecutor.class);
                final HmilyTransactionContext hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();
                if (Objects.nonNull(hmilyTransactionContext)) {
                    RpcMediator.getInstance().transmit(request::setAttachment,hmilyTransactionContext);
                }
                final Response response = caller.call(request);
                final HmilyParticipant hmilyParticipant = buildParticipant(hmilyTransactionContext, hmily, method, clazz, arguments, args);
                if (hmilyTransactionContext.getRole() == HmilyRoleEnum.INLINE.getCode()) {
                    hmilyTransactionExecutor.registerByNested(hmilyTransactionContext.getTransId(),
                            hmilyParticipant);
                } else {
                    hmilyTransactionExecutor.enlistParticipant(hmilyParticipant);
                }
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        } else {
            return caller.call(request);
        }
    }

    @SuppressWarnings("unchecked")
    private HmilyParticipant buildParticipant(final HmilyTransactionContext hmilyTransactionContext,
                                              final Hmily hmily, final Method method, final Class clazz,
                                              final Object[] arguments, final Class... args) throws HmilyRuntimeException {
        if (Objects.isNull(hmilyTransactionContext)
                || (HmilyActionEnum.TRYING.getCode() != hmilyTransactionContext.getAction())) {
            return null;
        }
        String confirmMethodName = hmily.confirmMethod();
        if (StringUtils.isBlank(confirmMethodName)) {
            confirmMethodName = method.getName();
        }
        String cancelMethodName = hmily.cancelMethod();
        if (StringUtils.isBlank(cancelMethodName)) {
            cancelMethodName = method.getName();
        }
        final PatternEnum pattern = hmily.pattern();
        SpringBeanUtils.getInstance().getBean(HmilyTransactionExecutor.class)
                .getCurrentTransaction().setPattern(pattern.getCode());
        HmilyInvocation confirmInvocation = new HmilyInvocation(clazz, confirmMethodName, args, arguments);
        HmilyInvocation cancelInvocation = new HmilyInvocation(clazz, cancelMethodName, args, arguments);
        return new HmilyParticipant(hmilyTransactionContext.getTransId(), confirmInvocation, cancelInvocation);
    }
}
