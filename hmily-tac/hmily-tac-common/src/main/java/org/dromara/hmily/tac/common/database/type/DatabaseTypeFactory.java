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

package org.dromara.hmily.tac.common.database.type;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.hmily.spi.ExtensionLoaderFactory;

/**
 * Database meta data dialect handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypeFactory {
    
    private static final Map<String, DatabaseType> DATABASE_TYPES = new HashMap<>();
    
    static {
        for (DatabaseType each : ExtensionLoaderFactory.loadAll(DatabaseType.class)) {
            DATABASE_TYPES.put(each.getName(), each);
        }
    }
    
    /**
     * Get database type by URL.
     *
     * @param url database URL
     * @return database type
     */
    public static DatabaseType getDatabaseTypeByURL(final String url) {
        return DATABASE_TYPES.values().stream().filter(each -> matchURLs(url, each)).findAny().orElse(DATABASE_TYPES.get("MySQL"));
    }
    
    private static boolean matchURLs(final String url, final DatabaseType databaseType) {
        return databaseType.getJdbcUrlPrefixes().stream().anyMatch(url::startsWith);
    }
}
