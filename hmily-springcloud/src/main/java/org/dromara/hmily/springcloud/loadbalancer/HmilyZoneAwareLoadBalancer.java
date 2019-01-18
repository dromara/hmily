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

package org.dromara.hmily.springcloud.loadbalancer;

import com.google.common.collect.Maps;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;
import com.netflix.loadbalancer.ServerListUpdater;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The type Hmily zone aware LoadBalancer.
 *
 * @author xiaoyu
 */
public class HmilyZoneAwareLoadBalancer extends ZoneAwareLoadBalancer<Server> {

    private static final Map<String, Server> SERVER_MAP = Maps.newConcurrentMap();

    /**
     * Instantiates a new Hmily zone aware loadbalancer.
     *
     * @param clientConfig      the client config
     * @param rule              the rule
     * @param ping              the ping
     * @param serverList        the server list
     * @param filter            the filter
     * @param serverListUpdater the server list updater
     */
    public HmilyZoneAwareLoadBalancer(final IClientConfig clientConfig,
                                      final IRule rule,
                                      final IPing ping,
                                      final ServerList<Server> serverList,
                                      final ServerListFilter<Server> filter,
                                      final ServerListUpdater serverListUpdater) {
        super(clientConfig, rule, ping, serverList, filter, serverListUpdater);
    }

    @Override
    public Server chooseServer(final Object key) {
        List<Server> serverList;
        serverList = super.getServerListImpl().getUpdatedListOfServers();
        serverList = super.getFilter().getFilteredListOfServers(serverList);
        if (null == serverList || serverList.isEmpty() || serverList.size() == 1) {
            return super.chooseServer(key);
        }
        final Server server = super.chooseServer(key);

        final HmilyTransactionContext hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();

        if (Objects.isNull(hmilyTransactionContext)) {
            return server;
        }

        final String transId = hmilyTransactionContext.getTransId();
        //if try
        if (hmilyTransactionContext.getAction() == HmilyActionEnum.TRYING.getCode()) {
            SERVER_MAP.put(transId, server);
            return server;
        }

        final Server oldServer = SERVER_MAP.get(transId);

        SERVER_MAP.remove(transId);

        if (Objects.nonNull(oldServer)) {
            for (Server s : serverList) {
                if (Objects.equals(s, oldServer)) {
                    return oldServer;
                }
            }
        }
        return server;
    }

}
