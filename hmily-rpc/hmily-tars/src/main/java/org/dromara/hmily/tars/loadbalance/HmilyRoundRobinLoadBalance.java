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
import com.qq.tars.client.cluster.ServantInvokerAliveChecker;
import com.qq.tars.client.cluster.ServantInvokerAliveStat;
import com.qq.tars.client.rpc.InvokerComparator;
import com.qq.tars.client.rpc.loadbalance.LoadBalanceHelper;
import com.qq.tars.common.util.CollectionUtils;
import com.qq.tars.rpc.common.InvokeContext;
import com.qq.tars.rpc.common.Invoker;
import com.qq.tars.rpc.common.LoadBalance;
import com.qq.tars.rpc.common.exc.NoInvokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * default hmily round robin load balance.
 *
 * @author tydhot
 */
public class HmilyRoundRobinLoadBalance<T> implements LoadBalance<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyRoundRobinLoadBalance.class);

    private final AtomicInteger sequence = new AtomicInteger();

    private final AtomicInteger staticWeightSequence = new AtomicInteger();

    private final ServantProxyConfig config;

    private final InvokerComparator comparator = new InvokerComparator();

    private volatile List<Invoker<T>> sortedInvokersCache;

    private volatile List<Invoker<T>> staticWeightInvokersCache;

    public HmilyRoundRobinLoadBalance(final ServantProxyConfig config) {
        this.config = config;
    }

    /**
     * Use load balancing to select invoker.
     *
     * @param invokeContext invokeContext
     * @return Invoker
     * @throws NoInvokerException NoInvokerException
     */
    @Override
    public Invoker<T> select(final InvokeContext invokeContext) throws NoInvokerException {
        List<Invoker<T>> staticWeightInvokers = staticWeightInvokersCache;
        if (staticWeightInvokers != null && !staticWeightInvokers.isEmpty()) {
            Invoker<T> invoker = staticWeightInvokers.get((staticWeightSequence.getAndIncrement() & Integer.MAX_VALUE) % staticWeightInvokers.size());
            if (invoker.isAvailable()) {
                return invoker;
            }
            ServantInvokerAliveStat stat = ServantInvokerAliveChecker.get(invoker.getUrl());
            if (stat.isAlive() || (stat.getLastRetryTime() + (config.getTryTimeInterval() * 1000)) < System.currentTimeMillis()) {
                LOGGER.info("try to use inactive invoker|" + invoker.getUrl().toIdentityString());
                stat.setLastRetryTime(System.currentTimeMillis());
                return invoker;
            }
        }
        List<Invoker<T>> sortedInvokers = sortedInvokersCache;
        if (CollectionUtils.isEmpty(sortedInvokers)) {
            throw new NoInvokerException("no such active connection invoker");
        }
        List<Invoker<T>> list = new ArrayList<Invoker<T>>();
        for (Invoker<T> invoker : sortedInvokers) {
            if (!invoker.isAvailable()) {
                ServantInvokerAliveStat stat = ServantInvokerAliveChecker.get(invoker.getUrl());
                if (stat.isAlive() || (stat.getLastRetryTime() + (config.getTryTimeInterval() * 1000)) < System.currentTimeMillis()) {
                    list.add(invoker);
                }
            } else {
                list.add(invoker);
            }
        }
        //TODO When all is not available. Whether to randomly extract one
        if (list.isEmpty()) {
            throw new NoInvokerException(config.getSimpleObjectName() + " try to select active invoker, size=" + sortedInvokers.size() + ", no such active connection invoker");
        }
        Invoker<T> invoker = list.get((sequence.getAndIncrement() & Integer.MAX_VALUE) % list.size());
        if (!invoker.isAvailable()) {
            LOGGER.info("try to use inactive invoker|" + invoker.getUrl().toIdentityString());
            ServantInvokerAliveChecker.get(invoker.getUrl()).setLastRetryTime(System.currentTimeMillis());
        }
        return HmilyLoadBalanceUtils.doSelect(invoker, sortedInvokersCache);
    }

    /**
     * Refresh local invoker.
     *
     * @param invokers invokers
     */
    @Override
    public void refresh(final Collection<Invoker<T>> invokers) {
        LOGGER.info("{} try to refresh RoundRobinLoadBalance's invoker cache, size= {} ", config.getSimpleObjectName(), CollectionUtils.isEmpty(invokers) ? 0 : invokers.size());
        if (CollectionUtils.isEmpty(invokers)) {
            sortedInvokersCache = null;
            staticWeightInvokersCache = null;
            return;
        }
        final List<Invoker<T>> sortedInvokersTmp = new ArrayList<Invoker<T>>(invokers);
        Collections.sort(sortedInvokersTmp, comparator);
        sortedInvokersCache = sortedInvokersTmp;
        staticWeightInvokersCache = LoadBalanceHelper.buildStaticWeightList(sortedInvokersTmp, config);
        LOGGER.info("{} refresh RoundRobinLoadBalance's invoker cache done, staticWeightInvokersCache size= {}, sortedInvokersCache size={}",
                config.getSimpleObjectName(),
                staticWeightInvokersCache == null || staticWeightInvokersCache.isEmpty() ? 0 : staticWeightInvokersCache.size(),
                sortedInvokersCache == null || sortedInvokersCache.isEmpty() ? 0 : sortedInvokersCache.size());
    }
}
