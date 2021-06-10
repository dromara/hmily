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

package org.dromara.hmily.config.zookeeper;

import java.io.ByteArrayInputStream;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hmily.common.utils.FileUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.ConfigScan;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.config.api.entity.HmilyDatabaseConfig;
import org.dromara.hmily.config.api.entity.HmilyFileConfig;
import org.dromara.hmily.config.api.entity.HmilyMetricsConfig;
import org.dromara.hmily.config.api.entity.HmilyMongoConfig;
import org.dromara.hmily.config.api.entity.HmilyRedisConfig;
import org.dromara.hmily.config.api.entity.HmilyServer;
import org.dromara.hmily.config.api.entity.HmilyZookeeperConfig;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.loader.ServerConfigLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author xiaoyu
 */
@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class ZookeeperConfigLoaderTest {
    
    @Mock
    private CuratorZookeeperClient client;
    
    @Before
    public void setUp() {
        String str = FileUtils.readYAML("hmily-zookeeper.yml");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes());
        Mockito.when(client.pull(any())).thenReturn(byteArrayInputStream);
    }
    
    @Test
    @Ignore
    public void realZookeeperLoad() {
        ZookeeperConfig remoteConfig = new ZookeeperConfig();
        remoteConfig.setServerList("127.0.0.1:2181");
        CuratorZookeeperClient client = CuratorZookeeperClient.getInstance(remoteConfig);
        client.persist("/hmily/xiaoyu", FileUtils.readYAML("hmily-zookeeper.yml"));
        ConfigScan.scan();
        ZookeeperConfigLoader configLoader = new ZookeeperConfigLoader(client);
        ServerConfigLoader loader = new ServerConfigLoader();
        loader.load(ConfigLoader.Context::new, (context, config) -> {
            System.out.println("config:---->" + config);
            if (config != null) {
                if (StringUtils.isNotBlank(config.getConfigMode())) {
                    String configMode = config.getConfigMode();
                    if (configMode.equals("zookeeper")) {
                        configLoader.load(context, (context1, config1) -> {
                        });
                    }
                }
            }
        });
    }
    
    @Test
    public void testZookeeperLoad() {
        ConfigScan.scan();
        ServerConfigLoader loader = new ServerConfigLoader();
        loader.load(ConfigLoader.Context::new, (context, config) -> {
            System.out.println("config:---->" + config);
            if (config != null) {
                if (StringUtils.isNotBlank(config.getConfigMode())) {
                    String configMode = config.getConfigMode();
                    if (configMode.equals("zookeeper")) {
                        new ZookeeperConfigLoader(client).load(context, this::assertTest);
                    }
                }
            }
        });
    }
    
    private void assertTest(final Supplier<ConfigLoader.Context> supplier, final ZookeeperConfig zookeeperConfig) {
        Assert.assertNotNull(zookeeperConfig);
        Assert.assertEquals(zookeeperConfig.prefix(), "remote.zookeeper");
        HmilyServer server = ConfigEnv.getInstance().getConfig(HmilyServer.class);
        Assert.assertNotNull(server);
        Assert.assertEquals(server.getConfigMode(), "nacos");
        HmilyConfig config = ConfigEnv.getInstance().getConfig(HmilyConfig.class);
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getAppName(), "xiaoyu");
        HmilyDatabaseConfig databaseConfig = ConfigEnv.getInstance().getConfig(HmilyDatabaseConfig.class);
        Assert.assertNotNull(databaseConfig);
        HmilyFileConfig fileConfig = ConfigEnv.getInstance().getConfig(HmilyFileConfig.class);
        Assert.assertNotNull(fileConfig);
        HmilyMetricsConfig metricsConfig = ConfigEnv.getInstance().getConfig(HmilyMetricsConfig.class);
        Assert.assertNotNull(metricsConfig);
        HmilyMongoConfig mongoConfig = ConfigEnv.getInstance().getConfig(HmilyMongoConfig.class);
        Assert.assertNotNull(mongoConfig);
        HmilyRedisConfig redisConfig = ConfigEnv.getInstance().getConfig(HmilyRedisConfig.class);
        Assert.assertNotNull(redisConfig);
        HmilyZookeeperConfig hmilyZookeeperConfig = ConfigEnv.getInstance().getConfig(HmilyZookeeperConfig.class);
        Assert.assertNotNull(hmilyZookeeperConfig);
    }
    
}
