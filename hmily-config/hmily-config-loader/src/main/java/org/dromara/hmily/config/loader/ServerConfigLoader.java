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

package org.dromara.hmily.config.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;

import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.entity.HmilyServer;
import org.dromara.hmily.config.api.exception.ConfigException;
import org.dromara.hmily.config.loader.property.PropertyKeySource;
import org.dromara.hmily.config.loader.yaml.YamlPropertyLoader;

/**
 * ParentConfigLoader .
 * Read basic BaseConfig information processing.
 *
 * @author xiaoyu
 */
public class ServerConfigLoader implements ConfigLoader<HmilyServer> {

    private final YamlPropertyLoader propertyLoader = new YamlPropertyLoader();

    @Override
    public void load(final Supplier<Context> context, final LoaderHandler<HmilyServer> handler) {
        String filePath = System.getProperty("hmily.conf");
        File configFile;
        if (StringUtils.isBlank(filePath)) {
            String dirPath = getDirGlobal();
            configFile = new File(dirPath);
            if (configFile.exists()) {
                filePath = dirPath;
            } else {
                //Mainly used for development environment。
                ClassLoader loader = ConfigLoader.class.getClassLoader();
                URL url = loader.getResource("hmily.yml");
                if (url != null) {
                    filePath = url.getFile();
                    configFile = new File(filePath);
                } else {
                    throw new ConfigException("ConfigLoader:loader config error,error file path:" + filePath);
                }
            }
        } else {
            configFile = new File(filePath);
            if (!configFile.exists()) {
                throw new ConfigException("ConfigLoader:loader config error,error file path:" + filePath);
            }
        }
        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            List<PropertyKeySource<?>> propertyKeySources = propertyLoader.load(filePath, inputStream);
            OriginalConfigLoader original = new OriginalConfigLoader();
            againLoad(() -> context.get().with(propertyKeySources, original), handler, HmilyServer.class);
        } catch (IOException e) {
            throw new ConfigException("ConfigLoader:loader config error,file path:" + filePath);
        }
    }

    /**
     * Get the current project path.
     *
     * @return Current project path
     */
    private String getDirGlobal() {
        String userDir = System.getProperty("user.dir");
        String fileName = "hmily.yml";
        return String.join(String.valueOf(File.separatorChar), userDir, fileName);
    }
}
