/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.tac.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Hmily resource manager.
 */
public class HmilyResourceManager {
    
    private static final Map<String, HmilyTacResource> DATASOURCE_CACHE = new ConcurrentHashMap<>();
    
    /**
     * Register.
     *
     * @param hmilyTacResource the hmily resource
     */
    public static void register(final HmilyTacResource hmilyTacResource) {
        DATASOURCE_CACHE.put(hmilyTacResource.getResourceId(), hmilyTacResource);
    }
    
    /**
     * Get hmily resource.
     *
     * @param resourceId the resource id
     * @return the hmily resource
     */
    public static HmilyTacResource get(final String resourceId) {
        return DATASOURCE_CACHE.get(resourceId);
    }
    
}
