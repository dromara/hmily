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

package org.dromara.hmily.config.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.Config;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.event.AddData;
import org.dromara.hmily.config.api.event.EventData;
import org.dromara.hmily.config.api.event.ModifyData;
import org.dromara.hmily.config.api.exception.ConfigException;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.loader.PropertyLoader;
import org.dromara.hmily.config.loader.properties.PropertiesLoader;
import org.dromara.hmily.config.loader.yaml.YamlPropertyLoader;
import org.dromara.hmily.spi.HmilySPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The type Nacos config loader.
 *
 * @author xiaoyu
 */
@HmilySPI("nacos")
public class NacosConfigLoader implements ConfigLoader<NacosConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosConfigLoader.class);

    private static final Map<String, PropertyLoader> LOADERS = new HashMap<>();

    private NacosClient client = new NacosClient();

    static {
        LOADERS.put("yml", new YamlPropertyLoader());
        LOADERS.put("properties", new PropertiesLoader());
    }

    /**
     * Instantiates a new Nacos config loader.
     */
    public NacosConfigLoader() {
    }

    /**
     * Instantiates a new Nacos config loader.
     *
     * @param client the client
     */
    public NacosConfigLoader(final NacosClient client) {
        this();
        this.client = client;
    }

    @Override
    public void load(final Supplier<Context> context, final LoaderHandler<NacosConfig> handler) {
        LoaderHandler<NacosConfig> nacosHandler = (c, config) -> nacosLoad(c, handler, config);
        againLoad(context, nacosHandler, NacosConfig.class);
    }

    @Override
    public void passive(final Supplier<Context> context, final PassiveHandler<Config> handler, final Config config) {
        if (config instanceof NacosPassiveConfig) {
            NacosPassiveConfig nacosPassiveConfig = (NacosPassiveConfig) config;
            String value = nacosPassiveConfig.getValue();
            if (StringUtils.isBlank(value)) {
                return;
            }
            PropertyLoader propertyLoader = LOADERS.get(nacosPassiveConfig.getFileExtension());
            if (propertyLoader == null) {
                throw new ConfigException("nacos.fileExtension setting error, The loader was not found");
            }
            InputStream inputStream = new ByteArrayInputStream(value.getBytes());
            Optional.of(inputStream)
                    .map(e -> propertyLoader.load(nacosPassiveConfig.fileName(), e))
                    .ifPresent(e -> e.forEach(x -> x.getKeys().forEach(t -> ConfigEnv.getInstance().stream()
                            .filter(c -> t.startsWith(c.prefix())).forEach(c -> {
                                Object o = c.getSource().get(t);
                                EventData data = null;
                                if (Objects.isNull(o)) {
                                    data = new AddData(t, x.getValue(t));
                                } else if (!Objects.equals(o, x.getValue(t))) {
                                    data = new ModifyData(t, x.getValue(t));
                                }
                                push(context, data);
                            }))));
        }
    }

    private void nacosLoad(final Supplier<Context> context, final LoaderHandler<NacosConfig> handler, final NacosConfig config) {
        if (config != null) {
            check(config);
            LOGGER.info("loader nacos config: {}", config);
            String fileExtension = config.getFileExtension();
            PropertyLoader propertyLoader = LOADERS.get(fileExtension);
            if (propertyLoader == null) {
                throw new ConfigException("nacos.fileExtension setting error, The loader was not found");
            }
            InputStream pull = client.pull(config);
            Optional.ofNullable(pull)
                    .map(e -> propertyLoader.load(config.fileName(), e))
                    .ifPresent(e -> context.get().getOriginal().load(() -> context.get().withSources(e), this::nacosFinish));
            handler.finish(context, config);
            try {
                client.addListener(context, (c1, c2) -> this.passive(c1, null, c2), config);
            } catch (NacosException e) {
                LOGGER.error("passive nacos remote started error....");
            }
        } else {
            throw new ConfigException("nacos config is null");
        }
    }

    private void nacosFinish(final Supplier<Context> context, final Config config) {
        LOGGER.info("nacos loader config {}:{}", config != null ? config.prefix() : "", config);
    }

    private void check(final NacosConfig config) {
        if (StringUtils.isBlank(config.getServer())) {
            throw new ConfigException("nacos.server is null");
        }
        if (StringUtils.isBlank(config.getDataId())) {
            throw new ConfigException("nacos.dataId is null");
        }
        if (StringUtils.isBlank(config.getFileExtension())) {
            throw new ConfigException("nacos.fileExtension is null");
        }
        if (StringUtils.isBlank(config.getGroup())) {
            throw new ConfigException("nacos.group is null");
        }
    }
}
