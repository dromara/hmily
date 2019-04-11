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
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
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
public class HmilyZoneAwareLoadBalancer extends ZoneAvoidanceRule {

    private static final Map<String, Server> SERVER_MAP = Maps.newConcurrentMap();

    public HmilyZoneAwareLoadBalancer() {
    }

    @Override
    public Server choose(final Object key) {
        List<Server> serverList = getLoadBalancer().getAllServers();
        if (null == serverList || serverList.isEmpty() || serverList.size() == 1) {
            return super.choose(key);
        }
        final Server server = super.choose(key);

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
