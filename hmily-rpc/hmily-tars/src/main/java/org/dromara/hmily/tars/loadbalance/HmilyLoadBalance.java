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

package org.dromara.hmily.tars.loadbalance;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.rpc.common.InvokeContext;
import com.qq.tars.rpc.common.Invoker;
import com.qq.tars.rpc.common.LoadBalance;
import com.qq.tars.rpc.common.exc.NoInvokerException;

import java.util.Collection;

/**
 * add hmily load balance.
 *
 * @author tydhot
 */
public class HmilyLoadBalance<T> implements LoadBalance<T> {

    private final HmilyRoundRobinLoadBalance<T> hmilyRoundRobinLoadBalance;

    private final ServantProxyConfig config;

    private volatile Collection<Invoker<T>> lastRefreshInvokers;

    private volatile HmilyHashLoadBalance<T> hashLoadBalance;

    private final Object hashLoadBalanceLock = new Object();

    private volatile HmilyConsistentHashLoadBalance<T> consistentHashLoadBalance;

    private final Object consistentHashLoadBalanceLock = new Object();

    public HmilyLoadBalance(final HmilyRoundRobinLoadBalance hmilyRoundRobinLoadBalance, final ServantProxyConfig config) {
        this.hmilyRoundRobinLoadBalance = hmilyRoundRobinLoadBalance;
        this.config = config;
    }

    @Override
    public Invoker<T> select(final InvokeContext invocation) throws NoInvokerException {
        long hash = Math.abs(StringUtils.convertLong(invocation.getAttachment(Constants.TARS_HASH), 0));
        long consistentHash = Math.abs(StringUtils.convertLong(invocation.getAttachment(Constants.TARS_CONSISTENT_HASH), 0));
        if (consistentHash > 0) {
            if (consistentHashLoadBalance == null) {
                synchronized (consistentHashLoadBalanceLock) {
                    if (consistentHashLoadBalance == null) {
                        HmilyConsistentHashLoadBalance<T> tmp = new HmilyConsistentHashLoadBalance<T>(config);
                        tmp.refresh(lastRefreshInvokers);
                        consistentHashLoadBalance = tmp;
                    }
                }
            }
            return consistentHashLoadBalance.select(invocation);
        }
        if (hash > 0) {
            if (hashLoadBalance == null) {
                synchronized (hashLoadBalanceLock) {
                    if (hashLoadBalance == null) {
                        HmilyHashLoadBalance<T> tmp = new HmilyHashLoadBalance<T>(config);
                        tmp.refresh(lastRefreshInvokers);
                        hashLoadBalance = tmp;
                    }
                }
            }
            return hashLoadBalance.select(invocation);
        }
        return hmilyRoundRobinLoadBalance.select(invocation);
    }

    @Override
    public void refresh(final Collection<Invoker<T>> invokers) {
        lastRefreshInvokers = invokers;
        synchronized (hashLoadBalanceLock) {
            if (hashLoadBalance != null) {
                hashLoadBalance.refresh(invokers);
            }
        }
        synchronized (consistentHashLoadBalanceLock) {
            if (consistentHashLoadBalance != null) {
                consistentHashLoadBalance.refresh(invokers);
            }
        }
        hmilyRoundRobinLoadBalance.refresh(invokers);
    }

}
