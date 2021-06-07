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

package org.dromara.hmily.spring.utils;

import java.util.Objects;
import org.dromara.hmily.common.utils.AssertUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * SpringBeanUtils.
 *
 * @author xiaoyu
 */
public enum SpringBeanUtils {
    
    /**
     * Instance spring bean utils.
     */
    INSTANCE;
    
    private ConfigurableApplicationContext cfgContext;
    
    /**
     * acquire spring bean.
     *
     * @param <T>  class
     * @param type type
     * @return bean bean
     */
    public <T> T getBean(final Class<T> type) {
        AssertUtils.notNull(type);
        try {
            return cfgContext.getBean(type);
        } catch (BeansException e) {
            try {
                return getByName(type);
            } catch (BeansException ex) {
                return null;
            }
        }
    }
    
    /**
     * Register bean.
     *
     * @param type   the type
     * @param object the object
     */
    public void registerBean(final Class<?> type, final Object object) {
        if (Objects.nonNull(cfgContext)) {
            cfgContext.getBeanFactory().registerSingleton(type.getSimpleName(), object);
        }
    }
    
    /**
     * set application context.
     *
     * @param cfgContext application context
     */
    public void setCfgContext(final ConfigurableApplicationContext cfgContext) {
        this.cfgContext = cfgContext;
    }
    
    private <T> T getByName(final Class<T> type) {
        return cfgContext.getBean(type.getSimpleName(), type);
    }
}
