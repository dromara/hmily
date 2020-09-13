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

package org.dromara.hmily.tac.sqlparser.shardingsphere;

import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;
import org.dromara.hmily.spi.HmilySPI;
import org.dromara.hmily.tac.sqlparser.model.statement.HmilyStatement;
import org.dromara.hmily.tac.sqlparser.spi.HmilySqlParserEngine;
import org.dromara.hmily.tac.sqlparser.spi.exception.SqlParserException;
import org.apache.shardingsphere.sql.parser.SQLParserEngineFactory;

/**
 * The type Sharding sphere sql parser engine.
 *
 * @author xiaoyu
 */
@HmilySPI("shardingSphere")
public class ShardingSphereSqlParserEngine implements HmilySqlParserEngine {
    
    @Override
    public HmilyStatement parser(final String sql, final String databaseType) throws SqlParserException {
        org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement shardingSphereSQLStatement =
                SQLParserEngineFactory.getSQLParserEngine(databaseType).parse(sql, false);
        
        if (shardingSphereSQLStatement instanceof UpdateStatement) {
            new org.dromara.hmily.tac.sqlparser.model.statement.dml.UpdateStatement();
            
            return null;
        } else if (shardingSphereSQLStatement instanceof InsertStatement) {
            return null;
        } else if (shardingSphereSQLStatement instanceof DeleteStatement) {
            return null;
        }
        return null;
    }
}
