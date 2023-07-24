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

package org.dromara.hmily.tac.sqlcompute;

import org.dromara.hmily.repository.spi.entity.HmilyDataSnapshot;
import org.dromara.hmily.tac.sqlcompute.exception.SQLComputeException;

import java.sql.Connection;
import java.util.List;

/**
 * Hmily SQL compute engine interface.
 *
 * @author zhaojun
 */
public interface HmilySQLComputeEngine {

    /**
     * Generate snapshot images.
     *
     * @param sql the sql
     * @param parameters parameters
     * @param connection connection
     * @param resourceId resource id
     * @return the hmily undo invocation
     * @throws SQLComputeException the SQL compute exception
     */
    HmilyDataSnapshot execute(String sql, List<Object> parameters, Connection connection, String resourceId) throws SQLComputeException;
}
