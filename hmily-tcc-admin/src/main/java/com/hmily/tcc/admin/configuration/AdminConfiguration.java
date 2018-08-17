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

package com.hmily.tcc.admin.configuration;

import com.hmily.tcc.admin.interceptor.AuthInterceptor;
import com.hmily.tcc.common.enums.SerializeEnum;
import com.hmily.tcc.common.serializer.KryoSerializer;
import com.hmily.tcc.common.serializer.ObjectSerializer;
import com.hmily.tcc.common.utils.ServiceBootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * AdminConfiguration.
 * @author xiaoyu(Myth)
 */
@Configuration
public class AdminConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(final InterceptorRegistry registry) {
                registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/**");
            }
        };
    }

    @Configuration
    static class SerializerConfiguration {

        private final Environment env;

        @Autowired
        SerializerConfiguration(final Environment env) {
            this.env = env;
        }

        @Bean
        public ObjectSerializer objectSerializer() {
            final SerializeEnum serializeEnum =
                    SerializeEnum.acquire(env.getProperty("recover.serializer.support"));
            final ServiceLoader<ObjectSerializer> objectSerializers =
                    ServiceBootstrap.loadAll(ObjectSerializer.class);
            return StreamSupport.stream(objectSerializers.spliterator(), false)
                    .filter(objectSerializer ->
                            Objects.equals(objectSerializer.getScheme(), serializeEnum.getSerialize()))
                    .findFirst().orElse(new KryoSerializer());
        }
    }

}
