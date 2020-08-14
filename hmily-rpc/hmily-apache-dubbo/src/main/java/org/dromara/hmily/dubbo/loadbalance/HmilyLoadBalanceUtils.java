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

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invoker;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;

/**
 * The type hmily load balance utils.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyLoadBalanceUtils {

    private static final Map<String, URL> URL_MAP = Maps.newConcurrentMap();
    
    public static <T> Invoker<T> doSelect(final Invoker<T> defaultInvoker, final List<Invoker<T>> invokers) {
        final HmilyTransactionContext hmilyTransactionContext = HmilyContextHolder.get();
        if (Objects.isNull(hmilyTransactionContext)) {
            return defaultInvoker;
        }
        //if try
        String key = defaultInvoker.getInterface().getName();
        if (hmilyTransactionContext.getAction() == HmilyActionEnum.TRYING.getCode()) {
            URL_MAP.put(key, defaultInvoker.getUrl());
            return defaultInvoker;
        }
        final URL orlUrl = URL_MAP.get(key);
        URL_MAP.remove(key);
        if (Objects.nonNull(orlUrl)) {
            for (Invoker<T> inv : invokers) {
                if (Objects.equals(inv.getUrl(), orlUrl)) {
                    return inv;
                }
            }
        }
        return defaultInvoker;
    }
}
