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

package org.dromara.hmily.dubbo.loadbalance;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.cluster.loadbalance.ConsistentHashLoadBalance;
import com.alibaba.dubbo.rpc.cluster.loadbalance.RandomLoadBalance;
import java.util.List;

/**
 * The type ConsistenHash Dubbo hmily load balance.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyConsistenHashDubboLoadBalance extends ConsistentHashLoadBalance {
    
    @Override
    protected <T> Invoker<T> doSelect(final List<Invoker<T>> invokers, final URL url, final Invocation invocation) {
        Invoker<T> defaultInvoker = super.doSelect(invokers, url, invocation);
        return HmilyLoadBalanceUtils.doSelect(defaultInvoker, invokers);
    }
}
