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

package org.dromara.hmily.tac.datasource.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.dromara.hmily.tac.datasource.HmilyDatasource;

/**
 * The type Hmily datasource manager.
 */
public class HmilyDatasourceManager {
    
    private static final Map<String, HmilyDatasource> DATASOURCE_CACHE = new ConcurrentHashMap<>();
    
    /**
     * Register.
     *
     * @param hmilyDatasource the hmily datasource
     */
    public static void register(final HmilyDatasource hmilyDatasource) {
        DATASOURCE_CACHE.put(hmilyDatasource.getResourceId(), hmilyDatasource);
    }
    
    /**
     * Get hmily datasource.
     *
     * @param resourceId the resource id
     * @return the hmily datasource
     */
    public static HmilyDatasource get(String resourceId) {
        return DATASOURCE_CACHE.get(resourceId);
    }
    
}
