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

package org.dromara.hmily.spring.boot.starter.parent.configuration;

import org.dromara.hmily.spring.HmilyApplicationContextAware;
import org.dromara.hmily.spring.annotation.RefererAnnotationBeanPostProcessor;
import org.dromara.hmily.spring.aop.SpringHmilyTransactionAspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;

/**
 * HmilyAutoConfiguration is spring boot starter handler.
 *
 * @author xiaoyu(Myth)
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class HmilyAutoConfiguration {
    
    /**
     * Hmily transaction aspect spring boot hmily transaction aspect.
     *
     * @return the spring boot hmily transaction aspect
     */
    @Bean
    public SpringHmilyTransactionAspect hmilyTransactionAspect() {
        return new SpringHmilyTransactionAspect();
    }
    
    /**
     * Referer annotation bean post processor referer annotation bean post processor.
     *
     * @return the referer annotation bean post processor
     */
    @Bean
    public RefererAnnotationBeanPostProcessor refererAnnotationBeanPostProcessor() {
        return new RefererAnnotationBeanPostProcessor();
    }
    
    /**
     * Hmily transaction bootstrap hmily application context aware.
     *
     * @return the hmily application context aware
     */
    @Bean
    @Qualifier("hmilyTransactionBootstrap")
    @Primary
    public HmilyApplicationContextAware hmilyTransactionBootstrap() {
        return new HmilyApplicationContextAware();
    }
}
