/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package org.dromara.hmily.config.api;

import java.util.List;

import org.dromara.hmily.spi.ExtensionLoaderFactory;

/**
 * ConfigScan
 * Scan config information to add.
 *
 * @author xiaoyu
 */
public final class ConfigScan {

    /**
     * Scan all config to add config env.
     */
    public static void scan() {
        List<Config> configs = ExtensionLoaderFactory.loadAll(Config.class);
        for (Config conf : configs) {
            ConfigEnv.getInstance().registerConfig(conf);
        }
    }
}
