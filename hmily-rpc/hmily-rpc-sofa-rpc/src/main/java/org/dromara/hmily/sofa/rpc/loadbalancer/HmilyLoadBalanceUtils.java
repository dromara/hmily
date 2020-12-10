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

import com.alipay.sofa.rpc.client.ProviderInfo;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;

/**
 * The type hmily load balance utils.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyLoadBalanceUtils {

    private static final Map<String, ProviderInfo> URL_MAP = Maps.newConcurrentMap();
    
    /**
     * Do select provider info.
     *
     * @param defaultProviderInfo the default provider info
     * @param providerInfos       the provider infos
     * @return the provider info
     */
    public static ProviderInfo doSelect(final ProviderInfo defaultProviderInfo, final List<ProviderInfo> providerInfos) {
        final HmilyTransactionContext hmilyTransactionContext = HmilyContextHolder.get();
        if (Objects.isNull(hmilyTransactionContext)) {
            return defaultProviderInfo;
        }
        //if try
        String key = defaultProviderInfo.getPath();
        if (hmilyTransactionContext.getAction() == HmilyActionEnum.TRYING.getCode()) {
            URL_MAP.put(key, defaultProviderInfo);
            return defaultProviderInfo;
        }
        final ProviderInfo oldProviderInfo = URL_MAP.get(key);
        URL_MAP.remove(key);
        if (Objects.nonNull(oldProviderInfo)) {
            for (ProviderInfo providerInfo : providerInfos) {
                if (Objects.equals(providerInfo, oldProviderInfo)) {
                    return oldProviderInfo;
                }
            }
        }
        return defaultProviderInfo;
    }
}
