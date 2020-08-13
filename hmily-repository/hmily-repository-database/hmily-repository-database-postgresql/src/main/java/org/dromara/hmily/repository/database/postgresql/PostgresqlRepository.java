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

package org.dromara.hmily.repository.database.postgresql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.dromara.hmily.config.HmilyDbConfig;
import org.dromara.hmily.repository.database.manager.AbstractHmilyDatabase;
import org.dromara.hmily.spi.HmilySPI;

/**
 * The type Postgresql repository.
 *
 * @author xiaoyu
 */
@HmilySPI(value = "postgresql")
@Slf4j
public class PostgresqlRepository extends AbstractHmilyDatabase {
    
    private static final String SQL_FILE_PATH = "postgresql/schema.sql";
    
    @Override
    protected String hmilyTransactionLimitSql(final int limit) {
        return SELECT_HMILY_TRANSACTION_DELAY + " limit " + limit;
    }
    
    @Override
    protected String hmilyParticipantLimitSql(final int limit) {
        return SELECTOR_HMILY_PARTICIPANT_WITH_DELAY_AND_APP_NAME_TRANS_TYPE + " limit " + limit;
    }
    
    @Override
    protected void initScript(final HmilyDbConfig hmilyDbConfig) throws Exception {
        String jdbcUrl = StringUtils.replace(hmilyDbConfig.getUrl(), "/hmily", "/");
        Connection conn = DriverManager.getConnection(jdbcUrl, hmilyDbConfig.getUsername(), hmilyDbConfig.getPassword());
        ScriptRunner runner = new ScriptRunner(conn);
        // doesn't print logger
        runner.setLogWriter(null);
        // doesn't print error
        runner.setErrorLogWriter(null);
        runner.setAutoCommit(false);
        runner.setSendFullScript(true);
        Resources.setCharset(StandardCharsets.UTF_8);
        Reader read = fillInfoToSqlFile(hmilyDbConfig.getUsername(), hmilyDbConfig.getPassword());
        runner.runScript(read);
        conn.commit();
        runner.closeConnection();
        conn.close();
    }
    
    @Override
    protected Object convertDataType(final Object params) {
        //https://jdbc.postgresql.org/documentation/head/8-date-time.html
        if (params instanceof java.util.Date) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(((Date) params).getTime()), ZoneId.systemDefault());
        }
        return params;
    }
    
    private Reader fillInfoToSqlFile(final String userName, final String password) throws IOException {
        final BufferedReader reader = new BufferedReader(Resources.getResourceAsReader(SQL_FILE_PATH));
        final StringBuilder builder = new StringBuilder();
        String str;
        while ((str = reader.readLine()) != null) {
            builder.append(str.replace("_user TEXT := 'userName'", "_user TEXT := '" + userName + "'")
            .replace("_password TEXT := 'password'", "_password TEXT := '" + password + "'"))
                    .append(System.lineSeparator());
        }
        reader.close();
        return new StringReader(builder.toString());
    }
}
