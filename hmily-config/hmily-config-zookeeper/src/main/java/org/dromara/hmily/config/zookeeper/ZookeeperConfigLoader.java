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

package org.dromara.hmily.config.zookeeper;


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.Config;
import org.dromara.hmily.config.api.exception.ConfigException;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.loader.PropertyLoader;
import org.dromara.hmily.config.loader.properties.PropertiesLoader;
import org.dromara.hmily.config.loader.yaml.YamlPropertyLoader;
import org.dromara.hmily.spi.HmilySPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Zookeeper config loader.
 *
 * @author xiaoyu
 */
@HmilySPI("zookeeper")
public class ZookeeperConfigLoader implements ConfigLoader<ZookeeperConfig> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfigLoader.class);
    
    private static final Map<String, PropertyLoader> LOADERS = new HashMap<>();
    
    private CuratorZookeeperClient client;
    
   static  {
       LOADERS.put("yml", new YamlPropertyLoader());
       LOADERS.put("properties", new PropertiesLoader());
    }
    
    public ZookeeperConfigLoader() {
    }
    
    public ZookeeperConfigLoader(final CuratorZookeeperClient client) {
        this();
        this.client = client;
    }
    
    @Override
    public void load(final Supplier<Context> context, final LoaderHandler<ZookeeperConfig> handler) {
        LoaderHandler<ZookeeperConfig> zookeeperLoad = (c, config) -> zookeeperLoad(c, handler, config);
        againLoad(context, zookeeperLoad, ZookeeperConfig.class);
    }
    
    private void zookeeperLoad(final Supplier<Context> context, final LoaderHandler<ZookeeperConfig> handler, final ZookeeperConfig config) {
        if (config != null) {
            check(config);
            if(Objects.isNull(client)) {
                client = CuratorZookeeperClient.getInstance(config);
            }
            InputStream result = client.pull(config.getPath());
            String fileExtension = config.getFileExtension();
            PropertyLoader propertyLoader = LOADERS.get(fileExtension);
            if (propertyLoader == null) {
                throw new ConfigException("zookeeper.fileExtension setting error, The loader was not found");
            }
            Optional.ofNullable(result)
                    .map(e -> propertyLoader.load("remote.zookeeper." + fileExtension , e))
                    .ifPresent(e -> context.get().getOriginal().load(() -> context.get().withSources(e), this::zookeeperFinish));
            handler.finish(context, config);
        } else {
            throw new ConfigException("zookeeper config is null");
        }
    }
    
    private void zookeeperFinish(final Supplier<Context> context, final Config config) {
        LOGGER.info("zookeeper loader config {}:{}", config != null ? config.prefix() : "", config);
    }
    
    private void check(ZookeeperConfig config) {
        if (StringUtils.isBlank(config.getServerList())) {
            throw new ConfigException("zookeeper server is null");
        }
    }
    
}
