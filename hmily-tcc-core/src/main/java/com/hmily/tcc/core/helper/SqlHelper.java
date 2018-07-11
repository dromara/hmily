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

package com.hmily.tcc.core.helper;

import com.hmily.tcc.common.utils.DbTypeUtils;

/**
 * SqlHelper.
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
        StringBuilder createTableSql = new StringBuilder();
        String dbType = DbTypeUtils.buildByDriverClassName(driverClassName);
        switch (dbType) {
            case "mysql":
                createTableSql
                        .append("CREATE TABLE IF NOT EXISTS `")
                        .append(tableName)
                        .append("` (")
                        .append("  `trans_id` varchar(64) NOT NULL,")
                        .append("  `target_class` varchar(256) ,")
                        .append("  `target_method` varchar(128) ,")
                        .append("  `confirm_method` varchar(128) ,")
                        .append("  `cancel_method` varchar(128) ,")
                        .append("  `retried_count` tinyint NOT NULL,")
                        .append("  `create_time` datetime NOT NULL,")
                        .append("  `last_time` datetime NOT NULL,")
                        .append("  `version` tinyint NOT NULL,")
                        .append("  `status` tinyint NOT NULL,")
                        .append("  `invocation` longblob,")
                        .append("  `role` tinyint NOT NULL,")
                        .append("  `pattern` tinyint,")
                        .append("  PRIMARY KEY (`trans_id`))");
                break;
            case "oracle":
                createTableSql
                        .append("CREATE TABLE IF NOT EXISTS `")
                        .append(tableName)
                        .append("` (")
                        .append("  `trans_id` varchar(64) NOT NULL,")
                        .append("  `target_class` varchar(256) ,")
                        .append("  `target_method` varchar(128) ,")
                        .append("  `confirm_method` varchar(128) ,")
                        .append("  `cancel_method` varchar(128) ,")
                        .append("  `retried_count` int(3) NOT NULL,")
                        .append("  `create_time` date NOT NULL,")
                        .append("  `last_time` date NOT NULL,")
                        .append("  `version` int(6) NOT NULL,")
                        .append("  `status` int(2) NOT NULL,")
                        .append("  `invocation` BLOB ,")
                        .append("  `role` int(2) NOT NULL,")
                        .append("  `pattern` int(2),")
                        .append("  PRIMARY KEY (`trans_id`))");
                break;
            case "sqlserver":
                createTableSql
                        .append("CREATE TABLE IF NOT EXISTS `")
                        .append(tableName)
                        .append("` (")
                        .append("  `trans_id` varchar(64) NOT NULL,")
                        .append("  `target_class` varchar(256) ,")
                        .append("  `target_method` varchar(128) ,")
                        .append("  `confirm_method` varchar(128) ,")
                        .append("  `cancel_method` varchar(128) ,")
                        .append("  `retried_count` int(3) NOT NULL,")
                        .append("  `create_time` datetime NOT NULL,")
                        .append("  `last_time` datetime NOT NULL,")
                        .append("  `version` int(6) NOT NULL,")
                        .append("  `status` int(2) NOT NULL,")
                        .append("  `invocation` varbinary ,")
                        .append("  `role` int(2) NOT NULL,")
                        .append("  `pattern` int(2),")
                        .append("  PRIMARY KEY (`trans_id`))");
                break;
            default:
                throw new RuntimeException("dbType类型不支持,目前仅支持mysql oracle sqlserver.");
        }
        return createTableSql.toString();
    }

}
