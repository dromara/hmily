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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;

import java.lang.reflect.Proxy;

public class FeignBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(final Object bean, @NonNull final String beanName) throws BeansException {
        //代理Feign
        Class<?> beanClass = bean.getClass();
        //findAnnotation保证找到接口的注解
        FeignClient feignClient = AnnotationUtils.findAnnotation(beanClass, FeignClient.class);
        if (feignClient != null) {
            return Proxy.newProxyInstance(beanClass.getClassLoader(), beanClass.getInterfaces(),
                    new FeignRequestInvocationHandler(bean));
        }
        return bean;
    }
}
