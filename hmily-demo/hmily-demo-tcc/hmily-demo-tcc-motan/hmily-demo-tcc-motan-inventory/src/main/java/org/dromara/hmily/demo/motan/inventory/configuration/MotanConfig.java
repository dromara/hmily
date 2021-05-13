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

package org.dromara.hmily.demo.motan.inventory.configuration;

import com.weibo.api.motan.config.springsupport.AnnotationBean;
import com.weibo.api.motan.config.springsupport.ProtocolConfigBean;
import com.weibo.api.motan.config.springsupport.RegistryConfigBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The MotanConfig.
 *
 * @author bbaiggey
 */
@Configuration
public class MotanConfig {
    
    /**
     * Motan annotation bean annotation bean.
     *
     * @return the annotation bean
     */
    @Bean
    @ConfigurationProperties(prefix = "hmily.motan.annotation")
    public AnnotationBean motanAnnotationBean() {
        return new AnnotationBean();
    }
    
    /**
     * Protocol config protocol config bean.
     *
     * @return the protocol config bean
     */
    @Bean(name = "hmilyMotan")
    @ConfigurationProperties(prefix = "hmily.motan.protocol")
    public ProtocolConfigBean protocolConfig() {
        return new ProtocolConfigBean();
    }
    
    /**
     * Registry config registry config bean.
     *
     * @return the registry config bean
     */
    @Bean(name = "hmilyRegistryConfig")
    @ConfigurationProperties(prefix = "hmily.motan.registry")
    public RegistryConfigBean registryConfig() {
        return new RegistryConfigBean();
    }
}
