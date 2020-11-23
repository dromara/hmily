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

package org.dromara.hmily.tac.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The enum Resource id utils.
 *
 * @author xiaoyu
 */
public enum ResourceIdUtils {
    
    /**
     * Instance resource id utils.
     */
    INSTANCE;
    
    private final Map<String, String> resourceIds = new ConcurrentHashMap<>();
    
    /**
     * Gets resource id.
     *
     * @param jdbcUrl the jdbc url
     * @return the resource id
     */
    public String getResourceId(final String jdbcUrl) {
        return resourceIds.computeIfAbsent(jdbcUrl, u -> u.contains("?") ? u.substring(0, u.indexOf('?')) : u);
    }
}
