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

package org.dromara.hmily.demo.springcloud.order.configuration;

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * MyConfiguration.
 *
 * @author xiaoyu
 */
@Configuration
@SuppressWarnings("all")
public class MyConfiguration {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate ();
    }


   /* @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .requestInterceptor(new HmilyFeignInterceptor())
                .invocationHandlerFactory(invocationHandlerFactory());
    }

    @Bean
    public InvocationHandlerFactory invocationHandlerFactory() {
        return (target, dispatch) -> {
            HmilyFeignHandler handler = new HmilyFeignHandler();
            //handler.setTarget(target);
            handler.setHandlers(dispatch);
            return handler;
        };
    }

    @Bean
    Request.Options feignOptions() {
        return new Request.Options(5000, 5000);
    }

    @Bean
    Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }*/
}
