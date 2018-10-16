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

package com.hmily.tcc.springcloud.feign;

import com.hmily.tcc.common.bean.context.TccTransactionContext;
import com.hmily.tcc.common.constant.CommonConstant;
import com.hmily.tcc.common.enums.TccRoleEnum;
import com.hmily.tcc.common.utils.GsonUtils;
import com.hmily.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import feign.RequestInterceptor;
import feign.RequestTemplate;
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
        final TccTransactionContext tccTransactionContext = TransactionContextLocal.getInstance().get();
        if (Objects.nonNull(tccTransactionContext)) {
            if (tccTransactionContext.getRole() == TccRoleEnum.LOCAL.getCode()) {
                tccTransactionContext.setRole(TccRoleEnum.INLINE.getCode());
            }
        }
        requestTemplate.header(CommonConstant.TCC_TRANSACTION_CONTEXT, GsonUtils.getInstance().toJson(tccTransactionContext));
    }

}
