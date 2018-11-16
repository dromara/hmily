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

package org.dromara.hmily.dubbo.interceptor;

import com.alibaba.dubbo.rpc.RpcContext;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.constant.CommonConstant;
import org.dromara.hmily.common.utils.GsonUtils;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.dromara.hmily.core.interceptor.HmilyTransactionInterceptor;
import org.dromara.hmily.core.service.HmilyTransactionAspectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The DubboHmilyTransactionInterceptor.
 *
 * @author xiaoyu
 */
@Component
public class DubboHmilyTransactionInterceptor implements HmilyTransactionInterceptor {

    private final HmilyTransactionAspectService hmilyTransactionAspectService;

    @Autowired
    public DubboHmilyTransactionInterceptor(final HmilyTransactionAspectService hmilyTransactionAspectService) {
        this.hmilyTransactionAspectService = hmilyTransactionAspectService;
    }

    @Override
    public Object interceptor(final ProceedingJoinPoint pjp) throws Throwable {
        final String context = RpcContext.getContext().getAttachment(CommonConstant.HMILY_TRANSACTION_CONTEXT);
        HmilyTransactionContext hmilyTransactionContext;
        if (StringUtils.isNoneBlank(context)) {
            hmilyTransactionContext = GsonUtils.getInstance().fromJson(context, HmilyTransactionContext.class);
            RpcContext.getContext().getAttachments().remove(CommonConstant.HMILY_TRANSACTION_CONTEXT);
        } else {
            hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();
        }
        return hmilyTransactionAspectService.invoke(hmilyTransactionContext, pjp);
    }
}
