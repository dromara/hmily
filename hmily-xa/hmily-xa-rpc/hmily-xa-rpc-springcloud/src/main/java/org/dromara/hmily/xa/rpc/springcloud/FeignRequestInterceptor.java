/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.xa.rpc.springcloud;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.context.XaParticipant;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.xa.core.XidImpl;
import org.dromara.hmily.xa.rpc.RpcXaProxy;
import org.springframework.core.Ordered;

import javax.transaction.xa.Xid;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 保证如果有context，就一定会设置
 * 保存的是远程rpc产生的，而有rpc则一定会commit或rollback
 */
class FeignRequestInterceptor implements RequestInterceptor, Ordered {

    @Override
    public void apply(RequestTemplate template) {
        RpcMediator.getInstance ().transmit (template::header, HmilyContextHolder.get ());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
