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

import lombok.Getter;

import java.util.Set;

/**
 * The type Property key source.
 *
 * @param <T> the type parameter
 * @author xiaoyu
 * @author sixh
 */
@Getter
public abstract class PropertyKeySource<T> {

    /**
     * The Name.
     */
    private final String name;

    /**
     * The Source.
     */
    private final T source;

    /**
     * Instantiates a new Property key source.
     *
     * @param name   the name
     * @param source the source
     */
    PropertyKeySource(final String name, final T source) {
        this.name = name;
        this.source = source;
    }

    /**
     * Gets value.
     *
     * @param key the key
     * @return the value
     */
    public abstract Object getValue(String key);

    /**
     * Gets keys.
     *
     * @return the keys
     */
    public abstract Set<String> getKeys();

    /**
     * Return original data.
     *
     * @return source
     */
    public T getSource() {
        return source;
    }
}
