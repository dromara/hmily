/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dromara.hmily.config.loader.property;

import java.util.Optional;
import lombok.Data;

/**
 * The type Config property.
 *
 * @author xiaoyu
 */
@Data
public class ConfigProperty {
    
    private PropertyName name;
    
    private Object value;
    
    /**
     * Instantiates a new Config property.
     *
     * @param name  the name
     * @param value the value
     */
    public ConfigProperty(final PropertyName name, final Object value) {
        this.name = name;
        this.value = value;
    }
    
    /**
     * Of config property.
     *
     * @param name  the name
     * @param value the value
     * @return the config property
     */
    public static ConfigProperty of(final PropertyName name, final Object value) {
        return Optional.ofNullable(value).map(cf -> new ConfigProperty(name, cf)).orElse(null);
    }
}
