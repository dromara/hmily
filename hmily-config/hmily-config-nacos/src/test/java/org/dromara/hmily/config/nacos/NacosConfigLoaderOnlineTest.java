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

import org.dromara.hmily.common.utils.FileUtils;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.ConfigScan;
import org.dromara.hmily.config.api.entity.*;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.loader.ServerConfigLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;

/**
 * The type Nacos config loader test.
 *
 * @author xiaoyu
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(NacosClient.class)
public class NacosConfigLoaderOnlineTest {

    /**
     * Test nacos load.
     */
    @Test
    public void testNacosLoad() throws InterruptedException {
        ConfigScan.scan();
        ServerConfigLoader loader = new ServerConfigLoader();
        NacosConfigLoader nacosConfigLoader = new NacosConfigLoader();
        loader.load(ConfigLoader.Context::new, (context, config) -> {
            if (config != null) {
                if (StringUtils.isNoneBlank(config.getConfigMode())) {
                    String configMode = config.getConfigMode();
                    if (configMode.equals("nacos")) {
                        nacosConfigLoader.load(context, this::assertTest);
                    }
                }
            }
        });
        Thread.sleep(Integer.MAX_VALUE);
    }

    private void assertTest(final Supplier<ConfigLoader.Context> supplier, final NacosConfig nacosConfig) {

    }
    
}
