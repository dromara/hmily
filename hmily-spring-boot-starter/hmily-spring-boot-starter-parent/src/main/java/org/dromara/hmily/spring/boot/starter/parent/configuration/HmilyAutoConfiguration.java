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
import org.dromara.hmily.spring.boot.starter.parent.config.HmilyConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
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
@EnableConfigurationProperties({HmilyConfigProperties.class})
@ComponentScan(basePackages = {"org.dromara.hmily"})
public class HmilyAutoConfiguration {

    private final HmilyConfigProperties hmilyConfigProperties;

    @Autowired(required = false)
    public HmilyAutoConfiguration(HmilyConfigProperties hmilyConfigProperties) {
        this.hmilyConfigProperties = hmilyConfigProperties;
    }

    @Bean
    @Qualifier("hmilyTransactionBootstrap")
    @Primary
    public HmilyApplicationContextAware hmilyTransactionBootstrap() {
        final HmilyApplicationContextAware contextAware = new HmilyApplicationContextAware();
        contextAware.setAppName(hmilyConfigProperties.getAppName());
        contextAware.setSerializer(hmilyConfigProperties.getSerializer());
        contextAware.setContextTransmittalMode(hmilyConfigProperties.getContextTransmittalMode());
        contextAware.setScheduledThreadMax(hmilyConfigProperties.getScheduledThreadMax());
        contextAware.setScheduledRecoveryDelay(hmilyConfigProperties.getScheduledRecoveryDelay());
        contextAware.setScheduledCleanDelay(hmilyConfigProperties.getScheduledCleanDelay());
        contextAware.setScheduledPhyDeletedDelay(hmilyConfigProperties.getScheduledPhyDeletedDelay());
        contextAware.setScheduledInitDelay(hmilyConfigProperties.getScheduledInitDelay());
        contextAware.setRecoverDelayTime(hmilyConfigProperties.getRecoverDelayTime());
        contextAware.setCleanDelayTime(hmilyConfigProperties.getCleanDelayTime());
        contextAware.setLimit(hmilyConfigProperties.getLimit());
        contextAware.setRetryMax(hmilyConfigProperties.getRetryMax());
        contextAware.setBufferSize(hmilyConfigProperties.getBufferSize());
        contextAware.setConsumerThreads(hmilyConfigProperties.getConsumerThreads());
        contextAware.setAsyncRepository(hmilyConfigProperties.isAsyncRepository());
        contextAware.setAutoSql(hmilyConfigProperties.isAutoSql());
        contextAware.setPhyDeleted(hmilyConfigProperties.isPhyDeleted());
        contextAware.setStoreDays(hmilyConfigProperties.getStoreDays());
        contextAware.setRepository(hmilyConfigProperties.getRepository());
        contextAware.setHmilyDbConfig(hmilyConfigProperties.getHmilyDbConfig());
        contextAware.setHmilyRedisConfig(hmilyConfigProperties.getHmilyRedisConfig());
        contextAware.setHmilyZookeeperConfig(hmilyConfigProperties.getHmilyZookeeperConfig());
        contextAware.setHmilyMongoConfig(hmilyConfigProperties.getHmilyMongoConfig());
        contextAware.setHmilyFileConfig(hmilyConfigProperties.getHmilyFileConfig());
        contextAware.setHmilyMetricsConfig(hmilyConfigProperties.getHmilyMetricsConfig());
        return contextAware;
    }
}
