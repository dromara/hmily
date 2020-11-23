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

package org.dromara.hmily.tac.metadata.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.hmily.tac.common.database.type.DatabaseType;
import org.dromara.hmily.tac.metadata.connection.MetaDataConnectionAdapter;
import org.dromara.hmily.tac.metadata.model.DataSourceMetaData;
import org.dromara.hmily.tac.metadata.model.TableMetaData;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


/**
 * Data source meta data loader.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceMetaDataLoader {
    
    /**
     * Load data source meta data.
     *
     * @param dataSource data source
     * @param databaseType database type
     * @return datasource metadata
     * @throws SQLException SQL exception
     */
    public static DataSourceMetaData load(final DataSource dataSource, final DatabaseType databaseType) throws SQLException {
        DataSourceMetaData result = new DataSourceMetaData();
        try (MetaDataConnectionAdapter connectionAdapter = new MetaDataConnectionAdapter(databaseType, dataSource.getConnection())) {
            for (String each : loadAllTableNames(connectionAdapter)) {
                Optional<TableMetaData> tableMetaData = TableMetaDataLoader.load(connectionAdapter, each, databaseType);
                tableMetaData.ifPresent(meta -> result.getTableMetaDataMap().put(each, meta));
            }
        }
        return result;
    }
    
    private static List<String> loadAllTableNames(final MetaDataConnectionAdapter connectionAdapter) throws SQLException {
        List<String> result = new LinkedList<>();
        try (ResultSet resultSet = connectionAdapter.getMetaData().getTables(connectionAdapter.getCatalog(),
            connectionAdapter.getSchema(), null, new String[]{"TABLE"})) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (!tableName.contains("$") && !tableName.contains("/")) {
                    result.add(tableName);
                }
            }
        }
        return result;
    }
}
