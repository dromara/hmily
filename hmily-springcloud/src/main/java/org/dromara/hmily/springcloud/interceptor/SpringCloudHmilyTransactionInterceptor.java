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

package org.dromara.hmily.springcloud.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.dromara.hmily.core.interceptor.HmilyTransactionInterceptor;
import org.dromara.hmily.core.service.HmilyTransactionAspectService;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;


/**
 * SpringCloudHmilyTransactionInterceptor.
 *
 * @author xiaoyu
 */
@Component
public class SpringCloudHmilyTransactionInterceptor implements HmilyTransactionInterceptor {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringCloudHmilyTransactionInterceptor.class);

    private final HmilyTransactionAspectService hmilyTransactionAspectService;

    @Autowired
    public SpringCloudHmilyTransactionInterceptor(final HmilyTransactionAspectService hmilyTransactionAspectService) {
        this.hmilyTransactionAspectService = hmilyTransactionAspectService;
    }

    @Override
    public Object interceptor(final ProceedingJoinPoint pjp) throws Throwable {
        HmilyTransactionContext hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();
        if (Objects.nonNull(hmilyTransactionContext)) {
            if (HmilyRoleEnum.START.getCode() == hmilyTransactionContext.getRole()) {
                hmilyTransactionContext.setRole(HmilyRoleEnum.SPRING_CLOUD.getCode());
            }
        } else {
            try {
                final RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
                hmilyTransactionContext = RpcMediator.getInstance().acquire(key -> {
                    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                    return request.getHeader(key);
                });
            } catch (IllegalStateException ex) {
                LogUtil.warn(LOGGER, () -> "can not acquire request info:" + ex.getLocalizedMessage());
            }
        }
        return hmilyTransactionAspectService.invoke(hmilyTransactionContext, pjp);
    }

}
