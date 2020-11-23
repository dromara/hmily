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

package org.dromara.hmily.tac.common.database.dialect;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.hmily.spi.ExtensionLoaderFactory;
import org.dromara.hmily.tac.common.database.type.DatabaseType;

/**
 * Database meta data dialect handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseMetaDataDialectHandlerFactory {
    
    /**
     * Find database meta data dialect handler.
     * 
     * @param databaseType database type
     * @return database meta data dialect handler
     */
    public static Optional<DatabaseMetaDataDialectHandler> findHandler(final DatabaseType databaseType) {
        return Optional.ofNullable(ExtensionLoaderFactory.load(DatabaseMetaDataDialectHandler.class, databaseType.getName()));
    }
}
