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

package org.dromara.hmily.brpc.loadbalance;

import com.baidu.brpc.client.CommunicationClient;
import com.google.common.collect.Maps;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The hmily brpc load balance utils referer annotation field .
 *
 * @author liu·yu
 */
public class HmilyLoadBalanceUtils {

    private static final Map<String, String> URL_MAP = Maps.newConcurrentMap();

    /**
     * do select client.
     *
     * @param defaultClient default client
     * @param instances all client
     * @return client
     */
    public static CommunicationClient doSelect(final CommunicationClient defaultClient,
                                               final List<CommunicationClient> instances) {
        HmilyTransactionContext hmilyTransactionContext = HmilyContextHolder.get();
        if (Objects.isNull(hmilyTransactionContext)) {
            return defaultClient;
        }
        //if try
        String key = defaultClient.getCommunicationOptions().getClientName();
        if (hmilyTransactionContext.getAction() == HmilyActionEnum.TRYING.getCode()) {
            URL_MAP.put(key, defaultClient.getServiceInstance().getIp());
            return defaultClient;
        }
        String ip = URL_MAP.get(key);
        URL_MAP.remove(key);
        if (Objects.nonNull(ip)) {
            for (CommunicationClient client : instances) {
                if (Objects.equals(client.getServiceInstance().getIp(), ip)) {
                    return client;
                }
            }
        }
        return defaultClient;
    }

}
