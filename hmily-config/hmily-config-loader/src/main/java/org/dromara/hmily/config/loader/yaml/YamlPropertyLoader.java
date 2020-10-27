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

package org.dromara.hmily.config.loader.yaml;

import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.loader.PropertyLoader;
import org.dromara.hmily.config.loader.property.MapPropertyKeySource;
import org.dromara.hmily.config.loader.property.PropertyKeySource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The type Yaml property loader.
 *
 * @author xiaoyu
 */
public class YamlPropertyLoader implements PropertyLoader {

    private static final String[] SUPPORT_FILE_SUFFIX = {".yml", ".yaml"};

    @Override
    public boolean checkFile(final String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return false;
        }
        return Arrays.stream(SUPPORT_FILE_SUFFIX).anyMatch(fileName::endsWith);
    }

    @Override
    public List<PropertyKeySource<?>> load(final String name, final InputStream resource) {
        if (!checkFile(name)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> loaded = new OriginTrackedYamlLoader(resource).load();
        if (loaded.isEmpty()) {
            return Collections.emptyList();
        }
        List<PropertyKeySource<?>> propertySources = new ArrayList<>(loaded.size());
        for (int i = 0; i < loaded.size(); i++) {
            propertySources.add(new MapPropertyKeySource(name + (loaded.size() != 1 ? " (document #" + i + ")" : ""), loaded.get(i)));
        }
        return propertySources;
    }
}
