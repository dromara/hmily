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

package org.dromara.hmily.core.service.rollback;

import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.bean.entity.HmilyInvocation;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.bean.entity.HmilyParticipant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.dromara.hmily.core.helper.SpringBeanUtils;
import org.dromara.hmily.core.service.HmilyRollbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * AsyncHmilyRollbackServiceImpl.
 * @author xiaoyu
 */
@Component
@SuppressWarnings("unchecked")
public class AsyncHmilyRollbackServiceImpl implements HmilyRollbackService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncHmilyRollbackServiceImpl.class);

    /**
     * 执行协调回滚方法.
     *
     * @param hmilyParticipantList 需要协调的资源集合
     */
    @Override
    public void execute(final List<HmilyParticipant> hmilyParticipantList) {
        try {
            if (CollectionUtils.isNotEmpty(hmilyParticipantList)) {
                final CompletableFuture[] cfs = hmilyParticipantList
                        .stream()
                        .map(participant ->
                                CompletableFuture.runAsync(() -> {
                                    HmilyTransactionContext context = new HmilyTransactionContext();
                                    context.setAction(HmilyActionEnum.CANCELING.getCode());
                                    context.setTransId(participant.getTransId());
                                    HmilyTransactionContextLocal.getInstance().set(context);
                                    try {
                                        executeParticipantMethod(participant.getCancelHmilyInvocation());
                                    } catch (Exception e) {
                                        LogUtil.error(LOGGER, "执行cancel方法异常：{}", e::getMessage);
                                        e.printStackTrace();
                                    }
                                }).whenComplete((v, e) -> HmilyTransactionContextLocal.getInstance().remove()))
                        .toArray(CompletableFuture[]::new);
                CompletableFuture.allOf(cfs).join();
            }
            LogUtil.debug(LOGGER, () -> "执行cancel方法成功！");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(LOGGER, "执行cancel方法异常：{}", e::getMessage);
        }

    }

    private void executeParticipantMethod(final HmilyInvocation hmilyInvocation) throws Exception {
        if (Objects.nonNull(hmilyInvocation)) {
            final Class clazz = hmilyInvocation.getTargetClass();
            final String method = hmilyInvocation.getMethodName();
            final Object[] args = hmilyInvocation.getArgs();
            final Class[] parameterTypes = hmilyInvocation.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            LogUtil.debug(LOGGER, "开始执行：{}", () -> clazz.getName() + " ;" + method);
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);
        }
    }
}
