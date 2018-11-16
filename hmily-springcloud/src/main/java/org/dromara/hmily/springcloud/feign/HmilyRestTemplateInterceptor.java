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

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.constant.CommonConstant;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.utils.GsonUtils;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * HmilyRestTemplateInterceptor.
 *
 * @author xiaoyu
 */
@Configuration
public class HmilyRestTemplateInterceptor implements RequestInterceptor {

    @Override
    public void apply(final RequestTemplate requestTemplate) {
        final HmilyTransactionContext hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();
        if (Objects.nonNull(hmilyTransactionContext)) {
            if (hmilyTransactionContext.getRole() == HmilyRoleEnum.LOCAL.getCode()) {
                hmilyTransactionContext.setRole(HmilyRoleEnum.INLINE.getCode());
            }
        }
        requestTemplate.header(CommonConstant.HMILY_TRANSACTION_CONTEXT, GsonUtils.getInstance().toJson(hmilyTransactionContext));
    }

}
