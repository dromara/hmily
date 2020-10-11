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
import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.rpc.common.InvokeContext;
import com.qq.tars.rpc.common.Invoker;
import com.qq.tars.rpc.common.LoadBalance;
import com.qq.tars.rpc.common.exc.NoInvokerException;
import com.qq.tars.support.log.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * add HmilyConsistentHashLoadBalance.
 *
 * @author tydhot
 */
public class HmilyConsistentHashLoadBalance<T> implements LoadBalance<T> {

    private static final Logger LOGGER = LoggerFactory.getClientLogger();

    private final ServantProxyConfig config;

    private final InvokerComparator comparator = new InvokerComparator();

    private volatile ConcurrentSkipListMap<Long, Invoker<T>> conHashInvokersCache;

    private volatile List<Invoker<T>> sortedInvokersCache;

    public HmilyConsistentHashLoadBalance(final ServantProxyConfig config) {
        this.config = config;
    }

    /**
     * Use load balancing to select invoker.
     *
     * @param invocation invocation
     * @return Invoker
     * @throws NoInvokerException NoInvokerException
     */
    @Override
    public Invoker<T> select(final InvokeContext invocation) throws NoInvokerException {
        long consistentHash = Math.abs(StringUtils.convertLong(invocation.getAttachment(Constants.TARS_CONSISTENT_HASH), 0));
        consistentHash = consistentHash & 0xFFFFFFFFL;
        ConcurrentSkipListMap<Long, Invoker<T>> conHashInvokers = conHashInvokersCache;
        if (conHashInvokers != null && !conHashInvokers.isEmpty()) {
            if (!conHashInvokers.containsKey(consistentHash)) {
                SortedMap<Long, Invoker<T>> tailMap = conHashInvokers.tailMap(consistentHash);
                if (tailMap.isEmpty()) {
                    consistentHash = conHashInvokers.firstKey();
                } else {
                    consistentHash = tailMap.firstKey();
                }
            }
            Invoker<T> invoker = conHashInvokers.get(consistentHash);
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(config.getSimpleObjectName() + " can't find active invoker using consistent hash loadbalance. try to use normal hash");
        }
        List<Invoker<T>> sortedInvokers = sortedInvokersCache;
        if (sortedInvokers == null || sortedInvokers.isEmpty()) {
            throw new NoInvokerException("no such active connection invoker");
        }
        List<Invoker<T>> list = new ArrayList<Invoker<T>>();
        for (Invoker<T> invoker : sortedInvokers) {
            if (!invoker.isAvailable()) {
                //Shield then call
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
        Invoker<T> invoker = list.get((int) (consistentHash % list.size()));
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
        LOGGER.info(config.getSimpleObjectName() + " try to refresh ConsistentHashLoadBalance's invoker cache, size=" + (invokers == null || invokers.isEmpty() ? 0 : invokers.size()));
        if (CollectionUtils.isEmpty(invokers)) {
            sortedInvokersCache = null;
            conHashInvokersCache = null;
            return;
        }
        List<Invoker<T>> sortedInvokersTmp = new ArrayList<>(invokers);
        sortedInvokersTmp.sort(comparator);
        sortedInvokersCache = sortedInvokersTmp;
        ConcurrentSkipListMap<Long, Invoker<T>> concurrentSkipListMap = new ConcurrentSkipListMap<Long, Invoker<T>>();
        LoadBalanceHelper.buildConsistentHashCircle(sortedInvokersTmp, config).forEach(concurrentSkipListMap::put);
        conHashInvokersCache = concurrentSkipListMap;
        LOGGER.info(config.getSimpleObjectName() + " refresh ConsistentHashLoadBalance's invoker cache done, conHashInvokersCache size="
                + (conHashInvokersCache == null || conHashInvokersCache.isEmpty() ? 0 : conHashInvokersCache.size())
                + ", sortedInvokersCache size=" + (sortedInvokersCache == null || sortedInvokersCache.isEmpty() ? 0 : sortedInvokersCache.size()));
    }
}
