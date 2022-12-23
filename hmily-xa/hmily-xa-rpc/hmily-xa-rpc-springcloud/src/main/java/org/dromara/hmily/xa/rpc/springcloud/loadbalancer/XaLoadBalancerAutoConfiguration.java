/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.xa.rpc.springcloud.loadbalancer;

import com.netflix.loadbalancer.ILoadBalancer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.netflix.ribbon.RibbonClientSpecification;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * 自动配置.
 */
@ConditionalOnClass(ILoadBalancer.class)
@Configuration
public class XaLoadBalancerAutoConfiguration implements InitializingBean {

    /**
     * Ribbon基于{@link SpringClientFactory}来实现配置隔离，
     * 要保证每种配置情况的{@link ILoadBalancer}都会被包装.
     */
    @Autowired
    private SpringClientFactory springClientFactory;

    /**
     * Register {@link SpringCloudXaLoadBalancer.TransactionEventListener} Bean.
     *
     * @return {@link SpringCloudXaLoadBalancer.TransactionEventListener} Bean
     */
    @Bean
    public SpringCloudXaLoadBalancer.TransactionEventListener xaTransactionEventListener() {
        return new SpringCloudXaLoadBalancer.TransactionEventListener();
    }

    /**
     * Ribbon基于{@link SpringClientFactory}来实现配置隔离，
     * 要保证每种配置情况的{@link ILoadBalancer}都会被包装.
     * 所以给{@link SpringClientFactory}添加一个默认config，它会注册{@link XaLoadBalancerBeanPostProcessor}.
     */
    @Override
    public void afterPropertiesSet() {
        //default. 开头的是每个app context公用的默认的配置类
        RibbonClientSpecification specification =
                new RibbonClientSpecification("default.SpringCloudXaBeanPostProcessor", new Class<?>[]{Config.class});
        springClientFactory.setConfigurations(Collections.singletonList(specification));
    }

    @Configuration
    static class Config {
        /**
         * 注册 {@link XaLoadBalancerBeanPostProcessor} Bean.
         *
         * @return {@link XaLoadBalancerBeanPostProcessor} Bean
         */
        @Bean
        public static XaLoadBalancerBeanPostProcessor xaLoadBalancerBeanPostProcessor() {
            return new XaLoadBalancerBeanPostProcessor();
        }
    }
}
