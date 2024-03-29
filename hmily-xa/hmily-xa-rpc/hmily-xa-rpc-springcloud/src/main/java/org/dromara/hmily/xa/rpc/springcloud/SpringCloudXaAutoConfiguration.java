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

package org.dromara.hmily.xa.rpc.springcloud;

import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置.
 */
@Configuration
@ConditionalOnClass(FeignClient.class)
public class SpringCloudXaAutoConfiguration {

    /**
     * Register {@link FeignRequestInterceptor} Bean.
     * @return {@link FeignRequestInterceptor} Bean
     */
    @Bean
    public RequestInterceptor hmilyXaInterceptor() {
        return new FeignRequestInterceptor();
    }

    /**
     * Register {@link FeignBeanPostProcessor} Bean.
     * @return {@link FeignBeanPostProcessor} Bean
     */
    @Bean
    public static FeignBeanPostProcessor hmilyXaPostProcessor() {
        return new FeignBeanPostProcessor();
    }

}
