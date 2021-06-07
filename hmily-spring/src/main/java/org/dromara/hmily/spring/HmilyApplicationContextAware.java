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

package org.dromara.hmily.spring;

import org.dromara.hmily.core.bootstrap.HmilyBootstrap;
import org.dromara.hmily.core.holder.SingletonHolder;
import org.dromara.hmily.core.provide.ObjectProvide;
import org.dromara.hmily.spring.provide.SpringBeanProvide;
import org.dromara.hmily.spring.utils.SpringBeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;

/**
 * The type Hmily application context aware.
 *
 * @author xiaoyu
 */
public class HmilyApplicationContextAware implements ApplicationContextAware, BeanFactoryPostProcessor {
    
    @Override
    public void setApplicationContext(@NonNull final ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtils.INSTANCE.setCfgContext((ConfigurableApplicationContext) applicationContext);
        SingletonHolder.INST.register(ObjectProvide.class, new SpringBeanProvide());
    }
    
    /**
     * Fix metric register happen before initialize.
     */
    @Override
    public void postProcessBeanFactory(@NonNull final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        HmilyBootstrap.getInstance().start();
    }
}
