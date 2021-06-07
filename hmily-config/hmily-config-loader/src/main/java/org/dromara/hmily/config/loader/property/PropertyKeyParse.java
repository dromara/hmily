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

package org.dromara.hmily.config.loader.property;

import java.util.Objects;

/**
 * PropertyKeyParse .
 * from {@linkplain PropertyName} to {@linkplain PropertyKey}
 *
 * @author xiaoyu
 */
public enum PropertyKeyParse {
    
    /**
     * Instance property key parse.
     */
    INSTANCE;

    private LastKey<PropertyName> lastKeyObj;

    private LastKey<String> lastKeyStr;
    
    /**
     * Parse property key [ ].
     *
     * @param propertyName the property name
     * @return the property key [ ]
     */
    public PropertyKey[] parse(final PropertyName propertyName) {
        LastKey<PropertyName> last = this.lastKeyObj;
        if (last != null && last.isFrom(propertyName)) {
            return last.getKeys();
        }
        String name = propertyName.getName();
        PropertyKey[] propertyKeys = {new PropertyKey(name, propertyName)};
        this.lastKeyObj = new LastKey<>(propertyName, propertyKeys);
        return propertyKeys;
    }
    
    /**
     * Parse property key [ ].
     *
     * @param propertyName the property name
     * @return the property key [ ]
     */
    public PropertyKey[] parse(final String propertyName) {
        // Use a local copy in case another thread changes things
        LastKey<String> last = this.lastKeyStr;
        if (last != null && last.isFrom(propertyName)) {
            return last.getKeys();
        }
        PropertyKey[] mapping = tryMap(propertyName);
        this.lastKeyStr = new LastKey<>(propertyName, mapping);
        return mapping;
    }

    private PropertyKey[] tryMap(final String propertyName) {
        PropertyName name = PropertyName.of(propertyName);
        if (!name.isEmpty()) {
            return new PropertyKey[]{new PropertyKey(propertyName, name)};
        }

        return new PropertyKey[0];
    }

    private static class LastKey<T> {

        private final T from;

        private final PropertyKey[] keys;
    
        /**
         * Instantiates a new Last key.
         *
         * @param from the from
         * @param keys the keys
         */
        LastKey(final T from, final PropertyKey[] keys) {
            this.from = from;
            this.keys = keys;
        }
    
        /**
         * Is from boolean.
         *
         * @param from the from
         * @return the boolean
         */
        boolean isFrom(final T from) {
            return Objects.equals(from, this.from);
        }
    
        /**
         * Get keys property key [ ].
         *
         * @return the property key [ ]
         */
        PropertyKey[] getKeys() {
            return this.keys;
        }
    }
}
