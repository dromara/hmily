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

package org.dromara.hmily.sofa.rpc.filter;

import com.alipay.sofa.rpc.context.RpcInternalContext;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.filter.AutoActive;
import com.alipay.sofa.rpc.filter.Filter;
import com.alipay.sofa.rpc.filter.FilterInvoker;
import org.dromara.hmily.core.mediator.RpcMediator;

/**
 * The type Hmily sofa rpc transaction provider filter.
 *
 * @author xiaoyu
 */
@Extension(value = "hmilySofaRpcTransactionProvider")
@AutoActive(providerSide = true)
public class HmilySofaRpcTransactionProviderFilter extends Filter {
    
    @Override
    public SofaResponse invoke(final FilterInvoker filterInvoker, final SofaRequest sofaRequest) throws SofaRpcException {
        RpcMediator.getInstance().getAndSet(sofaRequest::getRequestProp, RpcInternalContext.getContext()::setAttachment);
        return filterInvoker.invoke(sofaRequest);
    }
}
