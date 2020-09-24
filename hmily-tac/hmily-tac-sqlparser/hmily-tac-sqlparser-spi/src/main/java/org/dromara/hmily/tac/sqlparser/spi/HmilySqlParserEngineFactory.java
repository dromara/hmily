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

package org.dromara.hmily.tac.sqlparser.spi;

import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.spi.ExtensionLoaderFactory;

/**
 * The type Hmily sql parser engine factory.
 *
 * @author xiaoyu
 */
public class HmilySqlParserEngineFactory {
    
    private static volatile HmilySqlParserEngine hmilySqlParserEngine;
    
    /**
     * New instance hmily sql parser engine.
     *
     * @return the hmily sql parser engine
     */
    public static HmilySqlParserEngine newInstance() {
        if (hmilySqlParserEngine == null) {
            synchronized (HmilySqlParserEngineFactory.class) {
                if (hmilySqlParserEngine == null) {
                    HmilyConfig config = ConfigEnv.getInstance().getConfig(HmilyConfig.class);
                    hmilySqlParserEngine = ExtensionLoaderFactory.load(HmilySqlParserEngine.class, config.getSqlParserType());
                }
            }
        }
        return hmilySqlParserEngine;
    }
}
