/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.tac.sqlrevert.spi;

import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.spi.ExtensionLoaderFactory;

/**
 * The type Hmily sql revert engine factory.
 *
 * @author xiaoyu
 */
public class HmilySqlRevertEngineFactory {
    
    private static volatile HmilySqlRevertEngine hmilySqlRevertEngine;
    
    /**
     * New instance hmily sql revert engine.
     *
     * @return the hmily sql revert engine
     */
    public static HmilySqlRevertEngine newInstance() {
        if (hmilySqlRevertEngine == null) {
            synchronized (HmilySqlRevertEngineFactory.class) {
                if (hmilySqlRevertEngine == null) {
                    HmilyConfig config = ConfigEnv.getInstance().getConfig(HmilyConfig.class);
                    hmilySqlRevertEngine = ExtensionLoaderFactory.load(HmilySqlRevertEngine.class, config.getSqlRevert());
                }
            }
        }
        return hmilySqlRevertEngine;
    }
}
