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

package org.dromara.hmily.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HmilySPI Extend the processing.
 *
 * @author xiaoyu
 * @see ExtensionLoader
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HmilySPI {
    
    /**
     * Value string.
     *
     * @return the string
     */
    String value();
    
    /**
     * Order int, higher values are interpreted as lower priority.
     *
     * @return the int
     */
    int order() default 0;
    
    /**
     * Scope type scope type.
     *
     * @return the scope type
     */
    ScopeType scopeType() default ScopeType.SINGLETON;
}
