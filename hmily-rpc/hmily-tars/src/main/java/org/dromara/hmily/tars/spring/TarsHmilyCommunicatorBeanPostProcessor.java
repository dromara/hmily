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

package org.dromara.hmily.tars.spring;

import com.qq.tars.client.Communicator;
import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.protocol.annotation.Servant;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.spring.annotation.TarsClient;
import org.dromara.hmily.tars.loadbalance.HmilyLoadBalance;
import org.dromara.hmily.tars.loadbalance.HmilyRoundRobinLoadBalance;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * add HmilyCommunicatorBeanPostProcessor and override old tars's bean post processor.
 *
 * @author tydhot
 */
public class TarsHmilyCommunicatorBeanPostProcessor implements BeanPostProcessor, Ordered {

    private final Communicator communicator;

    public TarsHmilyCommunicatorBeanPostProcessor(final Communicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        Class clazz = bean.getClass();
        processFields(bean, clazz.getDeclaredFields());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    private void processFields(final Object bean, final Field[] declaredFields) {
        for (Field field : declaredFields) {
            TarsClient annotation = AnnotationUtils.getAnnotation(field, TarsClient.class);
            if (annotation == null) {
                continue;
            }

            if (field.getType().getAnnotation(Servant.class) == null) {
                throw new RuntimeException("[TARS] autowire client failed: target field is not  tars  client");
            }
            String objName = annotation.name();

            if (StringUtils.isEmpty(annotation.value())) {
                throw new RuntimeException("[TARS] autowire client failed: objName is empty");
            }
            ServantProxyConfig config = new ServantProxyConfig(objName);
            CommunicatorConfig communicatorConfig = ConfigurationManager.getInstance().getServerConfig().getCommunicatorConfig();
            config.setModuleName(communicatorConfig.getModuleName(), communicatorConfig.isEnableSet(), communicatorConfig.getSetDivision());
            if (StringUtils.isNotEmpty(communicatorConfig.getSetDivision())) {
                config.setSetDivision(communicatorConfig.getSetDivision());
            }
            if (StringUtils.isNotEmpty(annotation.setDivision())) {
                config.setSetDivision(communicatorConfig.getSetDivision());
                config.setEnableSet(annotation.enableSet());
            }
            config.setConnections(annotation.connections());
            config.setConnectTimeout(annotation.connectTimeout());
            config.setSyncTimeout(annotation.syncTimeout());
            config.setAsyncTimeout(annotation.asyncTimeout());
            config.setTcpNoDelay(annotation.tcpNoDelay());
            config.setCharsetName(annotation.charsetName());
            Object proxy = communicator.stringToProxy(field.getType(),
                    config,
                    new HmilyLoadBalance(new HmilyRoundRobinLoadBalance(config), config));
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, proxy);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
