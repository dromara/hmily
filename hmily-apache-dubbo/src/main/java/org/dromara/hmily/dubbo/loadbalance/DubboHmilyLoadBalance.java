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

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance;
import com.google.common.collect.Maps;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * The type Dubbo hmily load balance.
 *
 * @author xiaoyu(Myth)
 */
@SuppressWarnings("all")
public class DubboHmilyLoadBalance extends AbstractLoadBalance {

    private static final Map<String, URL> URL_MAP = Maps.newConcurrentMap();

    private final Random random = new Random();

    @Override
    protected <T> Invoker<T> doSelect(final List<Invoker<T>> invokers, final URL url, final Invocation invocation) {

        final Invoker<T> invoker = invokers.get(random.nextInt(invokers.size()));

        final HmilyTransactionContext hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();

        if (Objects.isNull(hmilyTransactionContext)) {
            return invoker;
        }

        final String transId = hmilyTransactionContext.getTransId();
        //if try
        if (hmilyTransactionContext.getAction() == HmilyActionEnum.TRYING.getCode()) {
            URL_MAP.put(transId, invoker.getUrl());
            return invoker;
        }

        final URL orlUrl = URL_MAP.get(transId);

        URL_MAP.remove(transId);

        if (Objects.nonNull(orlUrl)) {
            for (Invoker<T> inv : invokers) {
                if (Objects.equals(inv.getUrl(), orlUrl)) {
                    return inv;
                }
            }
        }
        return invoker;
    }
}
