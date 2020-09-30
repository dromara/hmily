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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.ConfigScan;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.config.api.event.EventConsumer;
import org.dromara.hmily.config.api.event.ModifyData;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.loader.ServerConfigLoader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * @author xiaoyu
 */
@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class ZookeeperConfigLoaderOnlineTest {

    @Test
    public void realZookeeperLoad() throws InterruptedException {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                changeData();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();


        ConfigScan.scan();
        ZookeeperConfigLoader configLoader = new ZookeeperConfigLoader();
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
        ConfigEnv.getInstance().addEvent(new EventConsumer<ModifyData>() {
            @Override
            public void accept(ModifyData data) {
                System.out.println(data);
            }

            @Override
            public String regex() {
                return "hmily.config.*";
            }
        });
    }

    private void changeData() throws IOException {
        ZookeeperConfig zookeeperConfig = buildZookeeperConfig();
        CuratorZookeeperClient client = CuratorZookeeperClient.getInstance(zookeeperConfig);
        InputStream resourceAsStream = getClass().getResourceAsStream("/hmily-zookeeper.yml");
        int available = resourceAsStream.available();
        byte[] bytes = IOUtils.readFully(resourceAsStream, available, false);
        client.persist(zookeeperConfig.getPath(), new String(bytes));
        System.out.println("init zookeeper resource completed. start update zookeeper resource...");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("xiaoyu", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getAppName());
        Assert.assertEquals("kryo", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getSerializer());
        Assert.assertEquals("threadLocal", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getContextTransmittalMode());

        resourceAsStream = getClass().getResourceAsStream("/hmily-zookeeper-update.yml");
        available = resourceAsStream.available();
        bytes = IOUtils.readFully(resourceAsStream, available, false);
        client.persist(zookeeperConfig.getPath(), new String(bytes));
        System.out.println("zookeeper resource updated.");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals("xiaoyu1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getAppName());
        Assert.assertEquals("kryo1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getSerializer());
        Assert.assertEquals("threadLocal1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getContextTransmittalMode());
    }

    private ZookeeperConfig buildZookeeperConfig() {
        ZookeeperConfig zookeeperConfig = new ZookeeperConfig();
        zookeeperConfig.setServerList("localhost:2181");
        zookeeperConfig.setPath("/hmily/config");
        return zookeeperConfig;
    }

    @Test
    public void testPull() throws IOException {
        ZookeeperConfig zookeeperConfig = buildZookeeperConfig();
        CuratorZookeeperClient client = CuratorZookeeperClient.getInstance(zookeeperConfig);
        InputStream resourceAsStream = getClass().getResourceAsStream("/hmily-zookeeper-update.yml");
        int available = resourceAsStream.available();
        byte[] bytes = IOUtils.readFully(resourceAsStream, available, false);
        String local = new String(bytes);

        client.persist(zookeeperConfig.getPath(), local);

        InputStream pull = client.pull(zookeeperConfig.getPath());
        available = pull.available();
        bytes = IOUtils.readFully(pull, available, false);
        Assert.assertNotNull(pull);
        Assert.assertEquals(local, new String(bytes));
    }


    private void assertTest(final Supplier<ConfigLoader.Context> supplier, final ZookeeperConfig zookeeperConfig) {

    }
    
}
