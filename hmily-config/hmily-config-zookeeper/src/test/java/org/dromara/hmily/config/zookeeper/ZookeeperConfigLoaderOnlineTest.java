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
import org.dromara.hmily.common.utils.FileUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.ConfigScan;
import org.dromara.hmily.config.api.entity.*;
import org.dromara.hmily.config.api.event.EventConsumer;
import org.dromara.hmily.config.api.event.EventData;
import org.dromara.hmily.config.api.event.ModifyData;
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

import java.io.ByteArrayInputStream;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author xiaoyu
 */
@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class ZookeeperConfigLoaderOnlineTest {

    @Test
    public void realZookeeperLoad() throws InterruptedException { ConfigScan.scan();
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
            public String properties() {
                return "hmily.config.*";
            }
        });
        Thread.sleep(Integer.MAX_VALUE);
    }

    
    private void assertTest(final Supplier<ConfigLoader.Context> supplier, final ZookeeperConfig zookeeperConfig) {

    }
    
}
