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

package org.dromara.hmily.core.mediator;

import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.constant.CommonConstant;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.utils.GsonUtils;
import org.dromara.hmily.common.utils.StringUtils;

import java.util.Objects;

/**
 * The type RpcMediator.
 *
 * @author xiaoyu(Myth)
 */
public class RpcMediator {

    private static final RpcMediator RPC_MEDIATOR = new RpcMediator();

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static RpcMediator getInstance() {
        return RPC_MEDIATOR;
    }


    /**
     * Transmit.
     *
     * @param rpcTransmit the rpc mediator
     * @param context     the context
     */
    public void transmit(final RpcTransmit rpcTransmit, final HmilyTransactionContext context) {
        if (Objects.nonNull(context)) {
            if (context.getRole() == HmilyRoleEnum.LOCAL.getCode()) {
                context.setRole(HmilyRoleEnum.INLINE.getCode());
            }
            rpcTransmit.transmit(CommonConstant.HMILY_TRANSACTION_CONTEXT,
                    GsonUtils.getInstance().toJson(context));
        }

    }

    /**
     * Acquire hmily transaction context.
     *
     * @param rpcAcquire the rpc acquire
     * @return the hmily transaction context
     */
    public HmilyTransactionContext acquire(RpcAcquire rpcAcquire) {
        HmilyTransactionContext hmilyTransactionContext = null;
        final String context = rpcAcquire.acquire(CommonConstant.HMILY_TRANSACTION_CONTEXT);
        if (StringUtils.isNoneBlank(context)) {
            hmilyTransactionContext = GsonUtils.getInstance().fromJson(context, HmilyTransactionContext.class);
        }
        return hmilyTransactionContext;
    }
}
