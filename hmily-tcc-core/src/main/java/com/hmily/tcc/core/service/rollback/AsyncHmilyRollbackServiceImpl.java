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

package com.hmily.tcc.core.service.rollback;

import com.hmily.tcc.common.enums.TccActionEnum;
import com.hmily.tcc.common.utils.LogUtil;
import com.hmily.tcc.common.bean.context.TccTransactionContext;
import com.hmily.tcc.common.bean.entity.Participant;
import com.hmily.tcc.common.bean.entity.TccInvocation;
import com.hmily.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.hmily.tcc.core.helper.SpringBeanUtils;
import com.hmily.tcc.core.service.HmilyRollbackService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
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
     * @param participantList 需要协调的资源集合
     */
    @Override
    public void execute(final List<Participant> participantList) {
        try {
            if (CollectionUtils.isNotEmpty(participantList)) {
                final CompletableFuture[] cfs = participantList
                        .stream()
                        .map(participant ->
                                CompletableFuture.runAsync(() -> {
                                    TccTransactionContext context = new TccTransactionContext();
                                    context.setAction(TccActionEnum.CANCELING.getCode());
                                    context.setTransId(participant.getTransId());
                                    TransactionContextLocal.getInstance().set(context);
                                    try {
                                        executeParticipantMethod(participant.getCancelTccInvocation());
                                    } catch (Exception e) {
                                        LogUtil.error(LOGGER, "执行cancel方法异常：{}", e::getMessage);
                                        e.printStackTrace();
                                    }
                                }).whenComplete((v, e) -> TransactionContextLocal.getInstance().remove()))
                        .toArray(CompletableFuture[]::new);
                CompletableFuture.allOf(cfs).join();
            }
            LogUtil.debug(LOGGER, () -> "执行cancel方法成功！");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(LOGGER, "执行cancel方法异常：{}", e::getMessage);
        }

    }

    private void executeParticipantMethod(final TccInvocation tccInvocation) throws Exception {
        if (Objects.nonNull(tccInvocation)) {
            final Class clazz = tccInvocation.getTargetClass();
            final String method = tccInvocation.getMethodName();
            final Object[] args = tccInvocation.getArgs();
            final Class[] parameterTypes = tccInvocation.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            LogUtil.debug(LOGGER, "开始执行：{}", () -> clazz.getName() + " ;" + method);
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);
        }
    }
}
