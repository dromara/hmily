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

package org.dromara.hmily.config.api;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Abstract config.
 *
 * @author xiaoyu
 */
public abstract class AbstractConfig implements Config {

    private volatile boolean isLoad;

    /**
     * Whether to passively receive push.
     */
    private boolean passive;

    /**
     * source map.
     */
    private Map<String, Object> source = new HashMap<>();

    @Override
    public void flagLoad() {
        isLoad = true;
    }

    @Override
    public boolean isLoad() {
        return isLoad;
    }

    @Override
    public boolean isPassive() {
        return passive;
    }

    /**
     * Sets passive.
     *
     * @param passive the passive
     */
    public void setPassive(final boolean passive) {
        this.passive = passive;
    }

    /**
     * Sets load.
     *
     * @param load the load
     */
    public void setLoad(final boolean load) {
        isLoad = load;
    }

    @Override
    public void setSource(final Map<String, Object> source) {
        this.source = source;
    }

    @Override
    public Map<String, Object> getSource() {
        return source;
    }
}
