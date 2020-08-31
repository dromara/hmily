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

package org.dromara.hmily.motan.field;

import com.weibo.api.motan.config.springsupport.annotation.MotanReferer;
import com.weibo.api.motan.proxy.RefererInvocationHandler;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.dromara.hmily.core.field.AnnotationField;
import org.dromara.hmily.spi.HmilySPI;

/**
 * The type Motan referer annotation field.
 *
 * @author xiaoyu
 */
@HmilySPI(value = "motan")
public class MotanRefererAnnotationField implements AnnotationField {
    
    @Override
    public boolean check(final Field field) {
        MotanReferer reference = field.getAnnotation(MotanReferer.class);
        return reference != null;
    }
}
