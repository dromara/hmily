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

package org.dromara.hmily.repository.database.sqlserver;

import java.io.Reader;
import java.sql.Connection;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.dromara.hmily.repository.database.manager.AbstractHmilyDatabase;
import org.dromara.hmily.spi.HmilySPI;

/**
 * The type Postgresql repository.
 *
 * @author xiaoyu
 */
@HmilySPI(value = "sqlserver")
@Slf4j
public class SqlserverRepository extends AbstractHmilyDatabase {
    
    @Override
    protected String sqlFilePath() {
        return "sqlserver/schema.sql";
    }

    @Override
    protected String hmilyTransactionLimitSql(final int limit) {
        return SELECT_HMILY_TRANSACTION_DELAY.replace("select","select top " + limit) ;
    }

    @Override
    protected String hmilyParticipantLimitSql(final int limit) {
        return SELECTOR_HMILY_PARTICIPANT_WITH_DELAY_AND_APP_NAME_TRANS_TYPE.replace("select","select top "+limit);
    }

    @Override
    protected void executeScript(final Connection conn,final String sqlPath) throws Exception {
        ScriptRunner runner = new ScriptRunner(conn);
        final String delimiter = "/";
        // doesn't print logger
        runner.setLogWriter(null);
        // doesn't print Error logger
       /* runner.setErrorLogWriter(null);*/
        runner.setAutoCommit(false);
        runner.setFullLineDelimiter(true);
        runner.setDelimiter(delimiter);
        try {
            Reader read = Resources.getResourceAsReader(sqlPath);
            runner.runScript(read);
            conn.commit();
        } catch (Exception ignored) {
        
        } finally {
            runner.closeConnection();
            conn.close();
        }
    }
    
    @Override
    protected Object convertDataType(final Object params) {
        if (params instanceof java.lang.Integer) {
            return ((Number)params).longValue();
        }
        return params;
    }
}
