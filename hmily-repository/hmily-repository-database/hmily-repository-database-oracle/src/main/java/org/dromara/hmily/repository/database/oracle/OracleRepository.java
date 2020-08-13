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

package org.dromara.hmily.repository.database.oracle;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.dromara.hmily.config.HmilyDbConfig;
import org.dromara.hmily.repository.database.manager.AbstractHmilyDatabase;
import org.dromara.hmily.spi.HmilySPI;

import java.io.Reader;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


/**
 * The type Oracle repository.
 *
 * @author xiaoyu
 */
@HmilySPI(value = "oracle")
@Slf4j
public class OracleRepository extends AbstractHmilyDatabase {
    
    private static final String SQL_FILE_PATH = "oracle/schema.sql";
    
    @Override
    protected String hmilyTransactionLimitSql(final int limit) {
        return SELECT_HMILY_TRANSACTION_DELAY + " and rownum <= " + limit;
    }
    
    @Override
    protected String hmilyParticipantLimitSql(final int limit) {
        return SELECTOR_HMILY_PARTICIPANT_WITH_DELAY_AND_APP_NAME_TRANS_TYPE + "and rownum <= " + limit;
    }
    
    @Override
    protected void initScript(final HmilyDbConfig hmilyDbConfig) throws Exception {
        String jdbcUrl = StringUtils.replace(hmilyDbConfig.getUrl(), "/hmily", "/");
        Connection conn = DriverManager.getConnection(jdbcUrl, hmilyDbConfig.getUsername(), hmilyDbConfig.getPassword());
        ScriptRunner runner = new ScriptRunner(conn);
        final String delimiter = "/";
        // doesn't print logger
        runner.setLogWriter(null);
        // doesn't print Error logger
        runner.setErrorLogWriter(null);
        runner.setAutoCommit(false);
        runner.setFullLineDelimiter(true);
        runner.setDelimiter(delimiter);
        try {
            Reader read = Resources.getResourceAsReader(SQL_FILE_PATH);
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
        if (params instanceof java.util.Date) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(((Date) params).getTime()), ZoneId.systemDefault());
        }
        if (params instanceof java.sql.Blob) {
            try {
                return ((Blob) params).getBytes(1, ((Number) ((Blob) params).length()).intValue());
            } catch (SQLException ex) {
                log.error("convertDataType-> fail to conver dataType Blob to byte[],{}", ex.getSQLState(), ex.getNextException());
            }
        }
        if (params instanceof java.math.BigDecimal) {
            return ((Number) params).longValue();
        }
        return params;
    }
}
