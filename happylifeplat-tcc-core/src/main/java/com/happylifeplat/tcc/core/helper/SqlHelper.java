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


public class SqlHelper {


    public static String buildCreateTableSql(String driverClassName, String tableName) {
        String createTableSql;
        String dbType = "mysql";
        if (driverClassName.contains("mysql")) {
            dbType = "mysql";
        } else if (driverClassName.contains("sqlserver")) {
            dbType = "sqlserver";
        } else if (driverClassName.contains("oracle")) {
            dbType = "oracle";
        }
        switch (dbType) {
            case "mysql": {
                createTableSql = "CREATE TABLE `" + tableName + "` (\n" +
                        "  `trans_id` varchar(64) NOT NULL,\n" +
                        "  `retried_count` int(3) NOT NULL,\n" +
                        "  `create_time` datetime NOT NULL,\n" +
                        "  `last_time` datetime NOT NULL,\n" +
                        "  `version` int(6) NOT NULL,\n" +
                        "  `status` int(2) NOT NULL,\n" +
                        "  `invocation` longblob,\n" +
                        "  `role` int(2) NOT NULL,\n" +
                        "  `pattern` int(2),\n" +
                        "  PRIMARY KEY (`trans_id`)\n" +
                        ")";
                break;
            }
            case "oracle": {
                createTableSql = "CREATE TABLE `" + tableName + "` (\n" +
                        "  `trans_id` varchar(64) NOT NULL,\n" +
                        "  `retried_count` int(3) NOT NULL,\n" +
                        "  `create_time` date NOT NULL,\n" +
                        "  `last_time` date NOT NULL,\n" +
                        "  `version` int(6) NOT NULL,\n" +
                        "  `status` int(2) NOT NULL,\n" +
                        "  `invocation` BLOB ,\n" +
                        "  `role` int(2) NOT NULL,\n" +
                        "  `pattern` int(2),\n" +
                        "  PRIMARY KEY (`trans_id`)\n" +
                        ")";
                break;
            }
            case "sqlserver": {
                createTableSql = "CREATE TABLE `" + tableName + "` (\n" +
                        "  `trans_id` varchar(64) NOT NULL,\n" +
                        "  `retried_count` int(3) NOT NULL,\n" +
                        "  `create_time` datetime NOT NULL,\n" +
                        "  `last_time` datetime NOT NULL,\n" +
                        "  `version` int(6) NOT NULL,\n" +
                        "  `status` int(2) NOT NULL,\n" +
                        "  `invocation` varbinary ,\n" +
                        "  `role` int(2) NOT NULL,\n" +
                        "  `pattern` int(2),\n" +
                        "  PRIMARY KEY (`trans_id`)\n" +
                        ")";
                break;
            }
            default: {
                throw new RuntimeException("dbType类型不支持,目前仅支持mysql oracle sqlserver.");
            }
        }
        return createTableSql;


    }

}
