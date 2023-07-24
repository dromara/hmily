/*
 * Copyright 2017-2021 Dromara.org

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

package org.dromara.hmily.tac.sqlparser.shardingsphere;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngineFactory;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleUpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLUpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerUpdateStatement;
import org.dromara.hmily.spi.HmilySPI;
import org.dromara.hmily.tac.common.database.type.DatabaseType;
import org.dromara.hmily.tac.sqlparser.model.common.statement.HmilyStatement;
import org.dromara.hmily.tac.sqlparser.shardingsphere.dialect.HmilyMySQLParserExecutor;
import org.dromara.hmily.tac.sqlparser.shardingsphere.dialect.HmilyOracleParserExecutor;
import org.dromara.hmily.tac.sqlparser.shardingsphere.dialect.HmilyPostgreSQLParserExecutor;
import org.dromara.hmily.tac.sqlparser.shardingsphere.dialect.HmilySQLServerParserExecutor;
import org.dromara.hmily.tac.sqlparser.spi.HmilySqlParserEngine;
import org.dromara.hmily.tac.sqlparser.spi.exception.SqlParserException;

/**
 * ShardingSphere SQL parser engine.
 *
 * @author xiaoyu
 */
@HmilySPI("shardingsphere")
@Slf4j
public final class ShardingSphereSqlParserEngine implements HmilySqlParserEngine {
    
    @Override
    public HmilyStatement parser(final String sql, final DatabaseType databaseType) throws SqlParserException {
        SQLStatement sqlStatement = SQLStatementParserEngineFactory.getSQLStatementParserEngine(databaseType.getName()).parse(sql, true);
        if (sqlStatement instanceof UpdateStatement) {
            return executeUpdateStatementParser((UpdateStatement) sqlStatement);
        } else if (sqlStatement instanceof InsertStatement) {
            return executeInsertStatementParser((InsertStatement) sqlStatement);
        } else if (sqlStatement instanceof DeleteStatement) {
            return executeDeleteStatementParser((DeleteStatement) sqlStatement);
        } else if (sqlStatement instanceof SelectStatement) {
            return executeSelectStatementParser((SelectStatement) sqlStatement);
        } else {
            throw new SqlParserException("Unsupported SQL Statement.");
        }
    }

    private HmilyStatement executeUpdateStatementParser(final UpdateStatement updateStatement) {
        if (updateStatement instanceof MySQLUpdateStatement) {
            HmilyMySQLParserExecutor hmilyMySQLParserExecutor = new HmilyMySQLParserExecutor();
            return hmilyMySQLParserExecutor.executeUpdateStatement(updateStatement);
        } else if (updateStatement instanceof PostgreSQLUpdateStatement) {
            HmilyPostgreSQLParserExecutor hmilyPostgreSQLParserExecutor = new HmilyPostgreSQLParserExecutor();
            return hmilyPostgreSQLParserExecutor.executeUpdateStatement(updateStatement);
        } else if (updateStatement instanceof OracleUpdateStatement) {
            HmilyOracleParserExecutor hmilyOracleParserExecutor = new HmilyOracleParserExecutor();
            return hmilyOracleParserExecutor.executeUpdateStatement(updateStatement);
        } else if (updateStatement instanceof SQLServerUpdateStatement) {
            HmilySQLServerParserExecutor hmilySQLServerParserExecutor = new HmilySQLServerParserExecutor();
            return hmilySQLServerParserExecutor.executeUpdateStatement(updateStatement);
        } else {
            throw new SqlParserException("Unsupported Dialect of Update Statement.");
        }
    }
    
    private HmilyStatement executeInsertStatementParser(final InsertStatement insertStatement) {
        if (insertStatement instanceof MySQLInsertStatement) {
            HmilyMySQLParserExecutor hmilyMySQLParserExecutor = new HmilyMySQLParserExecutor();
            return hmilyMySQLParserExecutor.executeInsertStatement(insertStatement);
        } else if (insertStatement instanceof PostgreSQLInsertStatement) {
            HmilyPostgreSQLParserExecutor hmilyPostgreSQLParserExecutor = new HmilyPostgreSQLParserExecutor();
            return hmilyPostgreSQLParserExecutor.executeInsertStatement(insertStatement);
        } else if (insertStatement instanceof OracleInsertStatement) {
            HmilyOracleParserExecutor hmilyOracleParserExecutor = new HmilyOracleParserExecutor();
            return hmilyOracleParserExecutor.executeInsertStatement(insertStatement);
        } else if (insertStatement instanceof SQLServerInsertStatement) {
            HmilySQLServerParserExecutor hmilySQLServerParserExecutor = new HmilySQLServerParserExecutor();
            return hmilySQLServerParserExecutor.executeInsertStatement(insertStatement);
        } else {
            throw new SqlParserException("Unsupported Dialect of Insert Statement.");
        }
    }
    
    private HmilyStatement executeDeleteStatementParser(final DeleteStatement deleteStatement) {
        if (deleteStatement instanceof MySQLDeleteStatement) {
            HmilyMySQLParserExecutor hmilyMySQLParserExecutor = new HmilyMySQLParserExecutor();
            return hmilyMySQLParserExecutor.executeDeleteStatement(deleteStatement);
        } else if (deleteStatement instanceof PostgreSQLDeleteStatement) {
            HmilyPostgreSQLParserExecutor hmilyPostgreSQLParserExecutor = new HmilyPostgreSQLParserExecutor();
            return hmilyPostgreSQLParserExecutor.executeDeleteStatement(deleteStatement);
        } else if (deleteStatement instanceof OracleDeleteStatement) {
            HmilyOracleParserExecutor hmilyOracleParserExecutor = new HmilyOracleParserExecutor();
            return hmilyOracleParserExecutor.executeDeleteStatement(deleteStatement);
        } else if (deleteStatement instanceof SQLServerDeleteStatement) {
            HmilySQLServerParserExecutor hmilySQLServerParserExecutor = new HmilySQLServerParserExecutor();
            return hmilySQLServerParserExecutor.executeDeleteStatement(deleteStatement);
        } else {
            throw new SqlParserException("Unsupported Dialect of Delete Statement.");
        }
    }

    private HmilyStatement executeSelectStatementParser(final SelectStatement selectStatement) {
        if (selectStatement instanceof MySQLSelectStatement) {
            HmilyMySQLParserExecutor hmilyMySQLParserExecutor = new HmilyMySQLParserExecutor();
            return hmilyMySQLParserExecutor.executeSelectStatement(selectStatement);
        } else if (selectStatement instanceof PostgreSQLSelectStatement) {
            HmilyPostgreSQLParserExecutor hmilyPostgreSQLParserExecutor = new HmilyPostgreSQLParserExecutor();
            return hmilyPostgreSQLParserExecutor.executeSelectStatement(selectStatement);
        } else if (selectStatement instanceof OracleSelectStatement) {
            HmilyOracleParserExecutor hmilyOracleParserExecutor = new HmilyOracleParserExecutor();
            return hmilyOracleParserExecutor.executeSelectStatement(selectStatement);
        } else if (selectStatement instanceof SQLServerSelectStatement) {
            HmilySQLServerParserExecutor hmilySQLServerParserExecutor = new HmilySQLServerParserExecutor();
            return hmilySQLServerParserExecutor.executeSelectStatement(selectStatement);
        } else {
            throw new SqlParserException("Unsupported Dialect of Select Statement.");
        }
    }
}
