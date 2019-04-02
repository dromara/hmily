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

import com.netflix.loadbalancer.IRule;
import org.dromara.hmily.springcloud.loadbalancer.HmilyZoneAwareLoadBalancer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * The type Hmily loadBalancer configuration.
 *
 * @author xiaoyu
 */
@Configuration
@ConditionalOnClass(IRule.class)
public class HmilyLoadBalancerConfiguration {

    /**
     * Hmily load balancer rule.
     *
     * @return the rule
     */
    @Bean
    @ConditionalOnProperty(name = "hmily.ribbon.rule.enabled")
    @Scope("prototype")
    public IRule hmilyLoadBalancer() {
        return new HmilyZoneAwareLoadBalancer();
    }

}
