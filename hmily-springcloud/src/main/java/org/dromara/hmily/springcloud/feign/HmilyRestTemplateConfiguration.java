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

package org.dromara.hmily.springcloud.feign;

import feign.InvocationHandlerFactory;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HmilyRestTemplateConfiguration.
 *
 * @author xiaoyu
 */
@Configuration
public class HmilyRestTemplateConfiguration {

    /**
     * Hmily rest template interceptor request interceptor.
     *
     * @return the request interceptor
     */
    @Bean
    public RequestInterceptor hmilyRestTemplateInterceptor() {
        return new HmilyRestTemplateInterceptor();
    }

    /**
     * build InvocationHandlerFactory.
     *
     * @return InvocationHandlerFactory
     */
    @Bean
    public InvocationHandlerFactory invocationHandlerFactory() {
        return (target, dispatch) -> {
            HmilyFeignHandler handler = new HmilyFeignHandler();
            handler.setHandlers(dispatch);
            return handler;
        };
    }

}
