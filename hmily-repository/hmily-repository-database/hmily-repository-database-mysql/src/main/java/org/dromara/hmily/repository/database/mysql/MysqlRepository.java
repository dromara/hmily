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

package org.dromara.hmily.repository.database.mysql;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.dromara.hmily.config.HmilyDbConfig;
import org.dromara.hmily.repository.database.manager.AbstractHmilyDatabase;
import org.dromara.hmily.spi.HmilySPI;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * The type Mysql repository.
 *
 * @author xiaoyu
 */
@HmilySPI(value = "mysql")
@Slf4j
public class MysqlRepository extends AbstractHmilyDatabase {
    
    private static final String SQL_FILE_PATH = "mysql/schema.sql";
    
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
        runner.setAutoCommit(false);
        Resources.setCharset(StandardCharsets.UTF_8);
        Reader read = Resources.getResourceAsReader(SQL_FILE_PATH);
        runner.runScript(read);
        conn.commit();
        runner.closeConnection();
        conn.close();
    }
    
    @Override
    protected Object convertDataType(final Object params) {
        return params;
    }
}
