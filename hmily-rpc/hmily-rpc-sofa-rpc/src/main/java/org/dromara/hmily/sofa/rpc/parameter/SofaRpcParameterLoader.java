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

package org.dromara.hmily.sofa.rpc.parameter;

import com.alipay.sofa.rpc.context.RpcInternalContext;
import java.util.Optional;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.core.mediator.RpcParameterLoader;
import org.dromara.hmily.spi.HmilySPI;

/**
 * The type Sofa rpc parameter loader.
 *
 * @author xiaoyu
 */
@HmilySPI(value = "sofa-rpc")
public class SofaRpcParameterLoader implements RpcParameterLoader {
    
    @Override
    public HmilyTransactionContext load() {
        return Optional.ofNullable(RpcMediator.getInstance().acquire(key -> {
            Object context = RpcInternalContext.getContext().getAttachment(key);
            return Optional.ofNullable(context).map(String::valueOf).orElse("");
        })).orElse(HmilyContextHolder.get());
    }
}
