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

package org.dromara.hmily.motan.interceptor;

import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.RpcContext;
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

import java.util.Map;
import java.util.Objects;

/**
 * The motan HmilyTransactionInterceptor.
 *
 * @author xiaoyu
 */
@Component
public class MotanHmilyTransactionInterceptor implements HmilyTransactionInterceptor {

    private final HmilyTransactionAspectService hmilyTransactionAspectService;

    @Autowired
    public MotanHmilyTransactionInterceptor(final HmilyTransactionAspectService hmilyTransactionAspectService) {
        this.hmilyTransactionAspectService = hmilyTransactionAspectService;
    }

    @Override
    public Object interceptor(final ProceedingJoinPoint pjp) throws Throwable {
        HmilyTransactionContext hmilyTransactionContext = null;
        final Request request = RpcContext.getContext().getRequest();
        if (Objects.nonNull(request)) {
            final Map<String, String> attachments = request.getAttachments();
            if (attachments != null && !attachments.isEmpty()) {
                String context = attachments.get(CommonConstant.HMILY_TRANSACTION_CONTEXT);
                if (StringUtils.isNoneBlank(context)) {
                    hmilyTransactionContext = GsonUtils.getInstance().fromJson(context, HmilyTransactionContext.class);
                    request.getAttachments().remove(CommonConstant.HMILY_TRANSACTION_CONTEXT);
                } else {
                    hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();
                }
            }
        } else {
            hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();
        }
        return hmilyTransactionAspectService.invoke(hmilyTransactionContext, pjp);
    }
}
