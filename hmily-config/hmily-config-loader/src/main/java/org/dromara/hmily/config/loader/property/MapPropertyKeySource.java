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

import java.util.Map;
import java.util.Set;

/**
 * MapPropertyKeySource.
 *
 * @author xiaoyu
 */
public class MapPropertyKeySource extends PropertyKeySource<Map<String, Object>> {
    
    /**
     * Instantiates a new Map property key source.
     *
     * @param name   the name
     * @param source the source
     */
    public MapPropertyKeySource(final String name, final Map<String, Object> source) {
        super(name, source);
    }

    @Override
    public Object getValue(final String key) {
        return getSource().get(key);
    }

    @Override
    public Set<String> getKeys() {
        return getSource().keySet();
    }
}
