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

package org.dromara.hmily.core.helper;

import org.dromara.hmily.common.constant.CommonConstant;
import org.dromara.hmily.common.utils.DbTypeUtils;

/**
 * SqlHelper.
 *
 * @author xiaoyu
 */
public class SqlHelper {

    /**
     * create table sql.
     *
     * @param driverClassName driverClassName .
     * @param tableName       table name .
     * @return sql.
     */
    public static String buildCreateTableSql(final String driverClassName, final String tableName) {
        String dbType = DbTypeUtils.buildByDriverClassName(driverClassName);
        switch (dbType) {
            case CommonConstant.DB_MYSQL:
                return buildMysql(tableName);
            case CommonConstant.DB_ORACLE:
                return buildOracle(tableName);
            case CommonConstant.DB_SQLSERVER:
                return buildSqlServer(tableName);
            case CommonConstant.DB_POSTGRESQL:
                return buildPostgresql(tableName);
            default:
                throw new RuntimeException("dbType not support ! The current support mysql oracle sqlserver postgresql.");
        }
    }

    private static String buildMysql(final String tableName) {
        return "CREATE TABLE IF NOT EXISTS `" +
                tableName +
                "` (" +
                "  `trans_id` varchar(64) NOT NULL," +
                "  `target_class` varchar(256) ," +
                "  `target_method` varchar(128) ," +
                "  `confirm_method` varchar(128) ," +
                "  `cancel_method` varchar(128) ," +
                "  `retried_count` tinyint NOT NULL," +
                "  `create_time` datetime NOT NULL," +
                "  `last_time` datetime NOT NULL," +
                "  `version` tinyint NOT NULL," +
                "  `status` tinyint NOT NULL," +
                "  `invocation` longblob," +
                "  `role` tinyint NOT NULL," +
                "  `pattern` tinyint," +
                "  PRIMARY KEY (`trans_id`))";
    }

    private static String buildOracle(final String tableName) {
        return "CREATE TABLE IF NOT EXISTS `" +
                tableName +
                "` (" +
                "  `trans_id` varchar(64) NOT NULL," +
                "  `target_class` varchar(256) ," +
                "  `target_method` varchar(128) ," +
                "  `confirm_method` varchar(128) ," +
                "  `cancel_method` varchar(128) ," +
                "  `retried_count` int(3) NOT NULL," +
                "  `create_time` date NOT NULL," +
                "  `last_time` date NOT NULL," +
                "  `version` int(6) NOT NULL," +
                "  `status` int(2) NOT NULL," +
                "  `invocation` BLOB ," +
                "  `role` int(2) NOT NULL," +
                "  `pattern` int(2)," +
                "  PRIMARY KEY (`trans_id`))";
    }

    private static String buildSqlServer(final String tableName) {
        return "CREATE TABLE IF NOT EXISTS `" +
                tableName +
                "` (" +
                "  `trans_id` varchar(64) NOT NULL," +
                "  `target_class` varchar(256) ," +
                "  `target_method` varchar(128) ," +
                "  `confirm_method` varchar(128) ," +
                "  `cancel_method` varchar(128) ," +
                "  `retried_count` int(3) NOT NULL," +
                "  `create_time` datetime NOT NULL," +
                "  `last_time` datetime NOT NULL," +
                "  `version` int(6) NOT NULL," +
                "  `status` int(2) NOT NULL," +
                "  `invocation` varbinary ," +
                "  `role` int(2) NOT NULL," +
                "  `pattern` int(2)," +
                "  PRIMARY KEY (`trans_id`))";
    }

    private static String buildPostgresql(final String tableName) {
        return " CREATE TABLE IF NOT EXISTS " +
                tableName +
                "(" +
                "  trans_id       VARCHAR(64) PRIMARY KEY," +
                "  target_class   VARCHAR(256)," +
                "  target_method  VARCHAR(128)," +
                "  confirm_method VARCHAR(128)," +
                "  cancel_method  VARCHAR(128)," +
                "  retried_count  SMALLINT    NOT NULL," +
                "  create_time    TIMESTAMP   NOT NULL," +
                "  last_time      TIMESTAMP   NOT NULL," +
                "  version        SMALLINT    NOT NULL," +
                "  status         SMALLINT    NOT NULL," +
                "  invocation     BYTEA," +
                "  role           SMALLINT    NOT NULL," +
                "  pattern        SMALLINT    NOT NULL" +
                ");";

    }

}
