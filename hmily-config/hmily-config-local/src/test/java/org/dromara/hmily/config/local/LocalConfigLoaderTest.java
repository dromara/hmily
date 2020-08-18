/*
 *
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.dromara.hmily.config.local;

import org.apache.commons.lang3.StringUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.ConfigScan;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.config.api.entity.HmilyServer;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.loader.ServerConfigLoader;
import org.junit.Test;

/**
 * Created by apa7 on 2019/10/10.
 */
public final class LocalConfigLoaderTest {

    @Test
    public void load() {
        ConfigScan.scan();
        ServerConfigLoader loader = new ServerConfigLoader();
        loader.load(ConfigLoader.Context::new, (context, config) -> {
            System.out.println("config:---->" + config);
            if (config != null) {
                if (StringUtils.isNotBlank(config.getConfigMode())) {
                    String configMode = config.getConfigMode();
                    if (configMode.equals("local")) {
                        new LocalConfigLoader().load(context, (context1, config1) -> System.out.println("config1:-->" + config1));
                    }
                }
            }
        });
        HmilyServer server = ConfigEnv.getInstance().getConfig(HmilyServer.class);
        HmilyConfig config = ConfigEnv.getInstance().getConfig(HmilyConfig.class);
        System.out.println(server);
        System.out.println(config);
    }
   
}