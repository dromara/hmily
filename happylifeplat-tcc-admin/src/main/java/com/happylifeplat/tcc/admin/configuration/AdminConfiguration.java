/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.happylifeplat.tcc.admin.configuration;

import com.happylifeplat.tcc.admin.interceptor.AuthInterceptor;

import com.happylifeplat.tcc.common.enums.SerializeEnum;
import com.happylifeplat.tcc.common.serializer.KryoSerializer;
import com.happylifeplat.tcc.common.serializer.ObjectSerializer;
import com.happylifeplat.tcc.common.utils.ServiceBootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/23 21:08
 * @since JDK 1.8
 */
@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class AdminConfiguration {


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/**");
            }
        };
    }


    static class SerializerConfiguration {

        private final Environment env;

        @Autowired
        public SerializerConfiguration(Environment env) {
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
                            Objects.equals(objectSerializer.getScheme(),
                                    serializeEnum.getSerialize())).findFirst().orElse(new KryoSerializer());

        }

    }

}
