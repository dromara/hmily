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

package org.dromara.hmily.brpc.parameter;

import com.baidu.brpc.RpcContext;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.core.mediator.RpcParameterLoader;
import org.dromara.hmily.spi.HmilySPI;

import java.util.Map;
import java.util.Optional;

/**
 * The hmily brpc parameter loader.
 *
 * @author liu·yu
 */
@HmilySPI(value = "brpc")
public class BrpcParameterLoader implements RpcParameterLoader {
    
    @Override
    public HmilyTransactionContext load() {
        return Optional.ofNullable(RpcMediator.getInstance().acquire(key -> {
            Map<String, Object> attachment = RpcContext.getContext().getRequestKvAttachment();
            if (attachment != null) {
                return String.valueOf(attachment.get(key));
            }
            return null;
        })).orElse(HmilyContextHolder.get());
    }
}
