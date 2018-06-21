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

package com.hmily.tcc.motan.filter;

import com.hmily.tcc.annotation.Tcc;
import com.hmily.tcc.annotation.TccPatternEnum;
import com.hmily.tcc.common.bean.context.TccTransactionContext;
import com.hmily.tcc.common.bean.entity.Participant;
import com.hmily.tcc.common.bean.entity.TccInvocation;
import com.hmily.tcc.common.constant.CommonConstant;
import com.hmily.tcc.common.enums.TccActionEnum;
import com.hmily.tcc.common.enums.TccRoleEnum;
import com.hmily.tcc.common.exception.TccRuntimeException;
import com.hmily.tcc.common.utils.GsonUtils;
import com.hmily.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.hmily.tcc.core.helper.SpringBeanUtils;
import com.hmily.tcc.core.service.executor.HmilyTransactionExecutor;
import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.core.extension.Activation;
import com.weibo.api.motan.core.extension.SpiMeta;
import com.weibo.api.motan.filter.Filter;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;
import com.weibo.api.motan.util.ReflectUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * MotanHmilyTransactionFilter.
 *
 * @author xiaoyu
 */
@SpiMeta(name = "motanTccTransactionFilter")
@Activation(key = { MotanConstants.NODE_TYPE_REFERER })
public class MotanHmilyTransactionFilter implements Filter {

    /**
     * 实现新浪的filter接口 rpc传参数.
     *
     * @param caller  caller
     * @param request 请求
     * @return Response
     */
    @Override
    @SuppressWarnings("unchecked")
    public Response filter(final Caller<?> caller, final Request request) {
        final String interfaceName = request.getInterfaceName();
        final String methodName = request.getMethodName();
        final Object[] arguments = request.getArguments();
        Class[] args = null;
        Method method = null;
        Tcc tcc = null;
        Class clazz = null;
        try {
            //他妈的 这里还要拿方法参数类型
            clazz = ReflectUtil.forName(interfaceName);
            final Method[] methods = clazz.getMethods();
            args = Stream.of(methods)
                    .filter(m -> m.getName().equals(methodName))
                    .findFirst()
                    .map(Method::getParameterTypes).get();
            method = clazz.getDeclaredMethod(methodName, args);
            tcc = method.getAnnotation(Tcc.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Objects.nonNull(tcc)) {
            try {
                final HmilyTransactionExecutor hmilyTransactionExecutor = SpringBeanUtils.getInstance().getBean(HmilyTransactionExecutor.class);
                final TccTransactionContext tccTransactionContext = TransactionContextLocal.getInstance().get();
                if (Objects.nonNull(tccTransactionContext)) {
                    request.setAttachment(CommonConstant.TCC_TRANSACTION_CONTEXT, GsonUtils.getInstance().toJson(tccTransactionContext));
                }
                final Response response = caller.call(request);
                final Participant participant = buildParticipant(tccTransactionContext, tcc, method, clazz, arguments, args);
                if (tccTransactionContext.getRole() == TccRoleEnum.PROVIDER.getCode()) {
                    hmilyTransactionExecutor.registerByNested(tccTransactionContext.getTransId(),
                            participant);
                } else {
                    hmilyTransactionExecutor.enlistParticipant(participant);
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
    private Participant buildParticipant(final TccTransactionContext tccTransactionContext,
                                         final Tcc tcc, final Method method, final Class clazz,
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
        //设置模式
        final TccPatternEnum pattern = tcc.pattern();
        SpringBeanUtils.getInstance().getBean(HmilyTransactionExecutor.class)
                .getCurrentTransaction().setPattern(pattern.getCode());
        TccInvocation confirmInvocation = new TccInvocation(clazz, confirmMethodName, args, arguments);
        TccInvocation cancelInvocation = new TccInvocation(clazz, cancelMethodName, args, arguments);
        //封装调用点
        return new Participant(tccTransactionContext.getTransId(), confirmInvocation, cancelInvocation);
    }
}
