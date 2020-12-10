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

package org.dromara.hmily.sofa.rpc.loadbalancer;

import com.alipay.sofa.rpc.bootstrap.ConsumerBootstrap;
import com.alipay.sofa.rpc.client.ProviderInfo;
import com.alipay.sofa.rpc.client.lb.LocalPreferenceLoadBalancer;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.ext.Extension;
import java.util.List;

/**
 * The type Hmily local pref sofa rpc load balance.
 *
 * @author xiaoyu(Myth)
 */
@Extension("hmilyLocalPref")
public class HmilyLocalPrefSofaRpcLoadBalancer extends LocalPreferenceLoadBalancer {
    
    /**
     * Instantiates a new Hmily local pref sofa rpc load balance.
     *
     * @param consumerBootstrap the consumer bootstrap
     */
    public HmilyLocalPrefSofaRpcLoadBalancer(final ConsumerBootstrap consumerBootstrap) {
        super(consumerBootstrap);
    }
    
    @Override
    public ProviderInfo doSelect(final SofaRequest invocation, final List<ProviderInfo> providerInfos) {
        ProviderInfo providerInfo = super.doSelect(invocation, providerInfos);
        return HmilyLoadBalanceUtils.doSelect(providerInfo, providerInfos);
    }
}
