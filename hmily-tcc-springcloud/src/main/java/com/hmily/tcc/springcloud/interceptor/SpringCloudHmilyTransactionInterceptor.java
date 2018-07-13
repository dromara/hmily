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
package com.hmily.tcc.springcloud.interceptor;

import com.hmily.tcc.common.bean.context.TccTransactionContext;
import com.hmily.tcc.common.constant.CommonConstant;
import com.hmily.tcc.common.utils.GsonUtils;
import com.hmily.tcc.core.interceptor.TccTransactionInterceptor;
import com.hmily.tcc.core.service.HmilyTransactionAspectService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


/**
 * SpringCloudHmilyTransactionInterceptor.
 * @author xiaoyu
 */
@Component
public class SpringCloudHmilyTransactionInterceptor implements TccTransactionInterceptor {

    private final HmilyTransactionAspectService hmilyTransactionAspectService;

    @Autowired
    public SpringCloudHmilyTransactionInterceptor(final HmilyTransactionAspectService hmilyTransactionAspectService) {
        this.hmilyTransactionAspectService = hmilyTransactionAspectService;
    }

    @Override
    public Object interceptor(final ProceedingJoinPoint pjp) throws Throwable {
        TccTransactionContext tccTransactionContext;
        //如果不是本地反射调用补偿
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = requestAttributes == null ? null : ((ServletRequestAttributes) requestAttributes).getRequest();
        String context = request == null ? null : request.getHeader(CommonConstant.TCC_TRANSACTION_CONTEXT);
        tccTransactionContext =  GsonUtils.getInstance().fromJson(context, TccTransactionContext.class);
        return hmilyTransactionAspectService.invoke(tccTransactionContext, pjp);
    }

}
