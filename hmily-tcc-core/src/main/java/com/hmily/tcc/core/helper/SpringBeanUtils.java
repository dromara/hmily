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

package com.hmily.tcc.core.helper;

import com.hmily.tcc.common.utils.AssertUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * SpringBeanUtils.
 * @author xiaoyu
 */
public final class SpringBeanUtils {

    private static final SpringBeanUtils INSTANCE = new SpringBeanUtils();

    private ConfigurableApplicationContext cfgContext;

    private SpringBeanUtils() {
        if (INSTANCE != null) {
            throw new Error("error");
        }
    }

    /**
     * get SpringBeanUtils.
     * @return SpringBeanUtils
     */
    public static SpringBeanUtils getInstance() {
        return INSTANCE;
    }

    /**
     * acquire spring bean.
     *
     * @param type type
     * @param <T>  class
     * @return bean
     */
    public <T> T getBean(final Class<T> type) {
        AssertUtils.notNull(type);
        T bean = null;
        try {
            bean = cfgContext.getBean (type);
        } catch (BeansException e) {
            bean = getByName (type);
        }
        return bean;
    }

    /**
     * 根据名称获取bean
     *
     * @param type
     * @param <T>
     * @return
     */
    private <T> T getByName(Class<T> type) {
        T bean;
        String className=type.getSimpleName ();
        bean = cfgContext.getBean (firstLowercase (firstDelete (className)) ,type);
        return bean;
    }

    /**
     * 首字母小写
     *
     * @param target
     * @return
     */
    private String firstLowercase(String target){
        if(StringUtils.isEmpty (target)){
            return target;
        }
        char[] targetChar = target.toCharArray ();
        targetChar[0]+=32;
        return String.valueOf (targetChar);
    }

    /**
     * 去除首字母
     *
     * @param target
     * @return
     */
    private static String firstDelete(String target){
        if(StringUtils.isEmpty (target)){
            return target;
        }
        return target.substring (1,target.length ());
    }

    /**
     * register bean in spring ioc.
     * @param beanName bean name
     * @param obj bean
     */
    public void registerBean(final String beanName, final Object obj) {
        AssertUtils.notNull(beanName);
        AssertUtils.notNull(obj);
        cfgContext.getBeanFactory().registerSingleton(beanName, obj);
    }

    /**
     * set application context.
     * @param cfgContext application context
     */
    public void setCfgContext(final ConfigurableApplicationContext cfgContext) {
        this.cfgContext = cfgContext;
    }
}
