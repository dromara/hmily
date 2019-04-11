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

import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import feign.RequestInterceptor;
import org.dromara.hmily.springcloud.feign.HmilyFeignBeanPostProcessor;
import org.dromara.hmily.springcloud.feign.HmilyFeignInterceptor;
import org.dromara.hmily.springcloud.hystrix.HmilyHystrixConcurrencyStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Hmily spring cloud configuration.
 *
 * @author xiaoyu(Myth)
 */
@Configuration
public class HmilyFeignConfiguration {

    /**
     * Hmily rest template interceptor request interceptor.
     *
     * @return the request interceptor
     */
    @Bean
    @Qualifier("hmilyFeignInterceptor")
    public RequestInterceptor hmilyFeignInterceptor() {
        return new HmilyFeignInterceptor();
    }

    /**
     * Feign post processor hmily feign bean post processor.
     *
     * @return the hmily feign bean post processor
     */
    @Bean
    public HmilyFeignBeanPostProcessor feignPostProcessor() {
        return new HmilyFeignBeanPostProcessor();
    }

    /**
     * Hystrix concurrency strategy hystrix concurrency strategy.
     *
     * @return the hystrix concurrency strategy
     */
    @Bean
    @ConditionalOnProperty(name = "feign.hystrix.enabled")
    public HystrixConcurrencyStrategy hystrixConcurrencyStrategy() {
        return new HmilyHystrixConcurrencyStrategy();
    }
}
