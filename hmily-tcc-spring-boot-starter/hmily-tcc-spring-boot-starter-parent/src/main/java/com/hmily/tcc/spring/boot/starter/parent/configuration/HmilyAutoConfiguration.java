/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.hmily.tcc.spring.boot.starter.parent.configuration;

import com.hmily.tcc.core.bootstrap.HmilyTransactionBootstrap;
import com.hmily.tcc.core.service.HmilyInitService;
import com.hmily.tcc.spring.boot.starter.parent.config.TccConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * HmilyAutoConfiguration is spring boot starter handler.
 *
 * @author xiaoyu(Myth)
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.hmily.tcc"})
public class HmilyAutoConfiguration {


    private final TccConfigProperties tccConfigProperties;

    @Autowired(required = false)
    public HmilyAutoConfiguration(TccConfigProperties tccConfigProperties) {
        this.tccConfigProperties = tccConfigProperties;
    }

    @Bean
    @Qualifier("hmilyTransactionBootstrap")
    public HmilyTransactionBootstrap hmilyTransactionBootstrap(HmilyInitService hmilyInitService) {
        final HmilyTransactionBootstrap hmilyTransactionBootstrap = new HmilyTransactionBootstrap(hmilyInitService);
        hmilyTransactionBootstrap.setBufferSize(tccConfigProperties.getBufferSize());
        hmilyTransactionBootstrap.setRetryMax(tccConfigProperties.getRetryMax());
        hmilyTransactionBootstrap.setRecoverDelayTime(tccConfigProperties.getRecoverDelayTime());
        hmilyTransactionBootstrap.setRepositorySuffix(tccConfigProperties.getRepositorySuffix());
        hmilyTransactionBootstrap.setRepositorySupport(tccConfigProperties.getRepositorySupport());
        hmilyTransactionBootstrap.setScheduledDelay(tccConfigProperties.getScheduledDelay());
        hmilyTransactionBootstrap.setScheduledThreadMax(tccConfigProperties.getScheduledThreadMax());
        hmilyTransactionBootstrap.setSerializer(tccConfigProperties.getSerializer());
        hmilyTransactionBootstrap.setTccFileConfig(tccConfigProperties.getTccFileConfig());
        hmilyTransactionBootstrap.setTccDbConfig(tccConfigProperties.getTccDbConfig());
        hmilyTransactionBootstrap.setTccRedisConfig(tccConfigProperties.getTccRedisConfig());
        hmilyTransactionBootstrap.setTccZookeeperConfig(tccConfigProperties.getTccZookeeperConfig());
        hmilyTransactionBootstrap.setTccMongoConfig(tccConfigProperties.getTccMongoConfig());
        hmilyTransactionBootstrap.setConsumerThreads(tccConfigProperties.getConsumerThreads());
        hmilyTransactionBootstrap.setLoadFactor(tccConfigProperties.getLoadFactor());
        hmilyTransactionBootstrap.setAsyncThreads(tccConfigProperties.getAsyncThreads());
        return hmilyTransactionBootstrap;
    }
}
