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

import org.apache.commons.lang3.StringUtils;
import org.dromara.hmily.common.utils.AssertUtils;
import org.dromara.hmily.core.provide.ObjectProvide;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * SpringBeanProvide.
 *
 * @author xiaoyu
 */
public final class SpringBeanProvide implements ObjectProvide {
    

    private ConfigurableApplicationContext cfgContext;
    
    
    @Override
    public Object provide(final Class<?> clazz) {
        return getBean(clazz);
    }

    /**
     * acquire spring bean.
     *
     * @param type type
     * @param <T>  class
     * @return bean
     */
    private <T> T getBean(final Class<T> type) {
        AssertUtils.notNull(type);
        T bean;
        try {
            bean = cfgContext.getBean(type);
        } catch (BeansException e) {
            bean = getByName(type);
        }
        return bean;
    }

    private <T> T getByName(final Class<T> type) {
        T bean;
        String className = type.getSimpleName();
        bean = cfgContext.getBean(firstLowercase(firstDelete(className)), type);
        return bean;
    }

    private String firstLowercase(final String target) {
        if (StringUtils.isEmpty(target)) {
            return target;
        }
        char[] targetChar = target.toCharArray();
        targetChar[0] += 32;
        return String.valueOf(targetChar);
    }

    private static String firstDelete(final String target) {
        if (StringUtils.isEmpty(target)) {
            return target;
        }
        return target.substring(1);
    }
    
    /**
     * set application context.
     *
     * @param cfgContext application context
     */
    public void setCfgContext(final ConfigurableApplicationContext cfgContext) {
        this.cfgContext = cfgContext;
    }
   
}
