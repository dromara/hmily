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

package org.dromara.hmily.springcloud.configuration;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;
import com.netflix.loadbalancer.ServerListUpdater;
import org.dromara.hmily.springcloud.loadbalancer.HmilyZoneAwareLoadBalancer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Hmily loadbalancer configuration.
 *
 * @author xiaoyu
 */
@Configuration
@ConditionalOnBean(RibbonClientConfiguration.class)
public class HmilyLoadBalancerConfiguration {

    /**
     * Hmily load balancer load balancer.
     *
     * @param config            the config
     * @param serverList        the server list
     * @param serverListFilter  the server list filter
     * @param rule              the rule
     * @param ping              the ping
     * @param serverListUpdater the server list updater
     * @return the load balancer
     */
    @Bean
    public ILoadBalancer hmilyLoadBalancer(final IClientConfig config,
                                           final ServerList<Server> serverList,
                                           final ServerListFilter<Server> serverListFilter,
                                           final IRule rule,
                                           final IPing ping,
                                           final ServerListUpdater serverListUpdater) {
        return new HmilyZoneAwareLoadBalancer(config, rule, ping, serverList,
                serverListFilter, serverListUpdater);
    }

}
