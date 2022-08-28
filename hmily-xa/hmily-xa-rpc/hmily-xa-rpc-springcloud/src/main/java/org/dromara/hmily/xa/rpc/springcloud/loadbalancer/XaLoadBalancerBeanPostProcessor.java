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

package org.dromara.hmily.xa.rpc.springcloud.loadbalancer;

import com.netflix.loadbalancer.ILoadBalancer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;

/**
 * 包装ILoadBalancer.
 */
public class XaLoadBalancerBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(final Object bean, @NonNull final String beanName) throws BeansException {
        if (hasInterface(bean.getClass(), ILoadBalancer.class)) {
            return new SpringCloudXaLoadBalancer((ILoadBalancer) bean);
        }

        return bean;
    }

    private boolean hasInterface(final Class<?> clazz, final Class<?> theInterface) {
        if (clazz == null || clazz.equals(Object.class)) {
            return false;
        }
        for (Class<?> anInterface : clazz.getInterfaces()) {
            if (anInterface.equals(theInterface)) {
                return true;
            }
        }
        return hasInterface(clazz.getSuperclass(), theInterface);
    }
}
