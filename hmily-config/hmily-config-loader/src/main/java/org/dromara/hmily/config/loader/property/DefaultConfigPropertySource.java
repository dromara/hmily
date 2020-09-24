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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The type Default config property source.
 *
 * @param <T> the type parameter
 * @author xiaoyu
 */
@RequiredArgsConstructor
public class DefaultConfigPropertySource<T> implements ConfigPropertySource {

    @Getter
    private final PropertyKeySource<T> keySource;
    
    @Getter
    private final PropertyKeyParse keyParse;
    
    @Override
    public ConfigProperty findProperty(final PropertyName propertyName) {
        if (propertyName.getElementSize() <= 0) {
            return null;
        }
        PropertyKey[] keys = getKeyParse().parse(propertyName);
        for (PropertyKey key : keys) {
            if (key.isAvailable(propertyName)) {
                ConfigProperty property = find(key);
                if (property != null) {
                    return property;
                }
            }
        }
        return null;
    }

    @Override
    public Stream<PropertyName> stream() {
        return getPropertyNames().stream();
    }

    @Override
    public boolean containsDescendantOf(final PropertyName propertyName) {
        List<PropertyName> propertyNames = getPropertyNames();
        return propertyNames.stream().anyMatch(e -> e.isAncestorOf(propertyName));
    }

    private List<PropertyName> getPropertyNames() {
        List<PropertyKey> propertyKeys = getPropertyKeys();
        List<PropertyName> names = new ArrayList<>(propertyKeys.size());
        propertyKeys.forEach(propertyKey -> names.add(propertyKey.getPropertyName()));
        return names;
    }

    private List<PropertyKey> getPropertyKeys() {
        Set<String> keys = getKeySource().getKeys();
        List<PropertyKey> propertyKeys = new ArrayList<>(keys.size());
        keys.forEach(key -> propertyKeys.addAll(Lists.newArrayList(getKeyParse().parse(key))));
        return propertyKeys;
    }

    private ConfigProperty find(final PropertyKey key) {
        String keyName = key.getKey();
        Object value = getKeySource().getValue(keyName);
        if (value == null) {
            return null;
        }
        PropertyName configurationPropertyName = key.getPropertyName();
        return ConfigProperty.of(configurationPropertyName, value);
    }
}
