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

package org.dromara.hmily.config.loader.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.dromara.hmily.config.api.exception.ConfigException;
import org.dromara.hmily.config.loader.PropertyLoader;
import org.dromara.hmily.config.loader.property.MapPropertyKeySource;
import org.dromara.hmily.config.loader.property.PropertyKeySource;

/**
 * PropertiesLoader .
 *
 * @author xiaoyu
 */
public class PropertiesLoader implements PropertyLoader {
    
    @Override
    public boolean checkFile(final String fileName) {
        String fileExtName = fileName.substring(fileName.lastIndexOf("."));
        String extName = ".properties";
        return extName.equals(fileExtName);
    }

    @Override
    public List<PropertyKeySource<?>> load(final String name, final InputStream stream) {
        if (!checkFile(name)) {
            return Collections.emptyList();
        }
        Map<String, Object> loaded;
        try {
            loaded = new OriginTrackedPropertiesLoader(stream).load();
        } catch (IOException e) {
            throw new ConfigException(e);
        }
        if (loaded == null || loaded.isEmpty()) {
            return Collections.emptyList();
        }
        List<PropertyKeySource<?>> propertySources = new ArrayList<>(loaded.size());
        propertySources.add(new MapPropertyKeySource(name, loaded));
        return propertySources;
    }
}
