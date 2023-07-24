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

import org.dromara.hmily.tac.sqlcompute.exception.SQLComputeException;
import org.dromara.hmily.tac.sqlcompute.impl.HmilyDeleteSQLComputeEngine;
import org.dromara.hmily.tac.sqlcompute.impl.HmilyInsertSQLComputeEngine;
import org.dromara.hmily.tac.sqlcompute.impl.HmilySelectSQLComputeEngine;
import org.dromara.hmily.tac.sqlcompute.impl.HmilyUpdateSQLComputeEngine;
import org.dromara.hmily.tac.sqlparser.model.common.statement.HmilyStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.mysql.dml.HmilyMySQLDeleteStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.mysql.dml.HmilyMySQLInsertStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.mysql.dml.HmilyMySQLSelectStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.mysql.dml.HmilyMySQLUpdateStatement;

/**
 * Hmily SQL compute engine factory.
 *
 * @author zhaojun
 */
public final class HmilySQLComputeEngineFactory {

    /**
     * Create new instance of hmily SQL compute engine.
     *
     * @param hmilyStatement hmily statement
     * @return Hmily SQL compute engine
     * @throws SQLComputeException SQL compute Exception
     */
    public static HmilySQLComputeEngine newInstance(final HmilyStatement hmilyStatement) throws SQLComputeException {
        if (hmilyStatement instanceof HmilyMySQLInsertStatement) {
            return new HmilyInsertSQLComputeEngine((HmilyMySQLInsertStatement) hmilyStatement);
        } else if (hmilyStatement instanceof HmilyMySQLUpdateStatement) {
            return new HmilyUpdateSQLComputeEngine((HmilyMySQLUpdateStatement) hmilyStatement);
        } else if (hmilyStatement instanceof HmilyMySQLDeleteStatement) {
            return new HmilyDeleteSQLComputeEngine((HmilyMySQLDeleteStatement) hmilyStatement);
        } else if (hmilyStatement instanceof HmilyMySQLSelectStatement) {
            return new HmilySelectSQLComputeEngine((HmilyMySQLSelectStatement) hmilyStatement);
        } else {
            throw new SQLComputeException(String.format("do not support hmily SQL compute yet, SQLStatement:{%s}.", hmilyStatement));
        }
    }
}
