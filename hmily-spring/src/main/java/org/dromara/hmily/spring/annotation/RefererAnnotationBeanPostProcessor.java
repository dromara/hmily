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

package org.dromara.hmily.spring.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.core.field.AnnotationField;
import org.dromara.hmily.core.field.DefaultAnnotationField;
import org.dromara.hmily.core.holder.SingletonHolder;
import org.dromara.hmily.spi.ExtensionLoaderFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

/**
 * The type Referer annotation bean post processor.
 *
 * @author xiaoyu
 */
public class RefererAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {
    
    private static AnnotationField annotationField;
    
    static {
        annotationField = Optional.ofNullable(ExtensionLoaderFactory.load(AnnotationField.class)).orElse(new DefaultAnnotationField());
    }
    
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
    
    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        if (isProxyBean(bean)) {
            clazz = AopUtils.getTargetClass(bean);
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (annotationField.check(field)) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    Object ref = field.get(bean);
                    Class<?> refClass = field.getType();
                    Method[] methods = refClass.getMethods();
                    boolean anyMatch = Stream.of(methods).anyMatch(method -> Objects.nonNull(method.getAnnotation(Hmily.class)));
                    if (anyMatch) {
                        SingletonHolder.INST.register(field.getType(), ref);
                    }
                }
            } catch (Exception e) {
                throw new BeanInitializationException("Failed to init spring bean at filed " + field.getName()
                        + " in class " + bean.getClass().getName(), e);
            }
        }
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }
    
    private boolean isProxyBean(final Object bean) {
        return AopUtils.isAopProxy(bean);
    }
}
