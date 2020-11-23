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

package org.dromara.hmily.tac.metadata;

import org.dromara.hmily.tac.common.HmilyTacResource;
import org.dromara.hmily.tac.common.database.type.DatabaseType;
import org.dromara.hmily.tac.metadata.loader.DataSourceMetaDataLoader;
import org.dromara.hmily.tac.metadata.model.DataSourceMetaData;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hmily metadata manager.
 *
 * @author zhaojun
 */
public class HmilyMetaDataManager {
    
    private static final Map<String, DataSourceMetaData> DATASOURCE_META_CACHE = new ConcurrentHashMap<>();
    
    /**
     * Register hmily metadata.
     *
     * @param hmilyTacResource the hmily resource
     * @param databaseType database type
     */
    public static void register(final HmilyTacResource hmilyTacResource, final DatabaseType databaseType) {
        DataSourceMetaData dataSourceMetaData;
        try {
            dataSourceMetaData = DataSourceMetaDataLoader.load(hmilyTacResource.getTargetDataSource(), databaseType);
        } catch (final SQLException ex) {
            throw new IllegalStateException("failed in loading datasource metadata into hmily");
        }
        DATASOURCE_META_CACHE.put(hmilyTacResource.getResourceId(), dataSourceMetaData);
    }
    
    /**
     * Get data source meta data.
     *
     * @param resourceId the resource id
     * @return data source metadata
     */
    public static DataSourceMetaData get(final String resourceId) {
        return DATASOURCE_META_CACHE.get(resourceId);
    }
    
}
