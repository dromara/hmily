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

package org.dromara.hmily.spring;

import org.dromara.hmily.config.HmilyConfig;
import org.dromara.hmily.core.bootstrap.HmilyBootstrap;
import org.dromara.hmily.core.holder.SingletonHolder;
import org.dromara.hmily.core.provide.ObjectProvide;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;

/**
 * The type Hmily application context aware.
 *
 * @author xiaoyu
 */
public class HmilyApplicationContextAware extends HmilyConfig implements ApplicationContextAware {
    
    @Override
    public void setApplicationContext(@NonNull final ApplicationContext applicationContext) throws BeansException {
        SpringBeanProvide objectProvide = new SpringBeanProvide();
        objectProvide.setCfgContext((ConfigurableApplicationContext) applicationContext);
        SingletonHolder.INST.register(ObjectProvide.class, objectProvide);
        HmilyBootstrap.getInstance().start(this);
    }
}
