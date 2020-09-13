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

package org.dromara.hmily.config.local;

import org.dromara.hmily.config.api.Config;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.spi.HmilySPI;

import java.util.function.Supplier;

/**
 * LocalConfigLoader .
 * Loading local yml file processing.
 *
 * @author xiaoyu
 */
@HmilySPI("local")
public class LocalConfigLoader implements ConfigLoader<Config> {

    @Override
    public void load(final Supplier<Context> context, final LoaderHandler<Config> handler) {
        ConfigLoader<Config> original = context.get().getOriginal();
        original.load(context, handler);
    }
}
