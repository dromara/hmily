/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.happylifeplat.tcc.core.helper;


import com.happylifeplat.tcc.common.utils.DbTypeUtils;

/**
 * @author xiaoyu
 */
public class SqlHelper {


    public static String buildCreateTableSql(String driverClassName, String tableName) {
        StringBuilder createTableSql = new StringBuilder();
        String dbType = DbTypeUtils.buildByDriverClassName(driverClassName);
        switch (dbType) {
            case "mysql": {
                createTableSql
                        .append("CREATE TABLE `")
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
                        .append("  `invocation` longblob,")
                        .append("  `role` int(2) NOT NULL,")
                        .append("  `pattern` int(2),")
                        .append("  PRIMARY KEY (`trans_id`))");
                break;
            }
            case "oracle": {
                createTableSql
                        .append("CREATE TABLE `")
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
            }
            case "sqlserver": {
                createTableSql
                        .append("CREATE TABLE `")
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
            }
            default: {
                throw new RuntimeException("dbType类型不支持,目前仅支持mysql oracle sqlserver.");
            }
        }
        return createTableSql.toString();


    }

}
