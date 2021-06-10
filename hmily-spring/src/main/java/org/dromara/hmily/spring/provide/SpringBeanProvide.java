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

package org.dromara.hmily.spring.provide;

import org.dromara.hmily.core.holder.SingletonHolder;
import org.dromara.hmily.core.provide.ObjectProvide;
import org.dromara.hmily.spring.utils.SpringBeanUtils;

/**
 * SpringBeanProvide.
 *
 * @author xiaoyu
 */
public final class SpringBeanProvide implements ObjectProvide {
    
    @Override
    public Object provide(final Class<?> clazz) {
        Object bean = SpringBeanUtils.INSTANCE.getBean(clazz);
        if (null == bean) {
            return SingletonHolder.INST.get(clazz);
        }
        return bean;
    }
}
