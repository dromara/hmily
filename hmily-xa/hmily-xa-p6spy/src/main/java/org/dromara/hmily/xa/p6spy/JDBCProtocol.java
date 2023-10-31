/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dromara.hmily.xa.p6spy;


import java.util.Arrays;
import java.util.Optional;

/**
 * The enum Jdbc protocol.
 */
public enum JDBCProtocol {
    
    /**
     * mysql.
     */
    MYSQL("mysql", "jdbc:mysql:"),
    
    /**
     * Mariadb jdbc protocol.
     */
    MARIADB("mariadb", "jdbc:mariadb:"),
    
    /**
     * Sql server jdbc protocol.
     */
    SQL_SERVER("sqlserver", "jdbc:sqlserver:"),
    
    /**
     * Postgresql jdbc protocol.
     */
    POSTGRESQL("postgresql", "jdbc:postgresql:"),
    
    /**
     * Oracle jdbc protocol.
     */
    ORACLE("oracle", "jdbc:oracle:"),
    ;
    
    private String dbType;
    
    private String protocol;
    
    /**
     * 构建一个协议对象.
     *
     * @param dbType   dbType.
     * @param protocol protocol.
     */
    JDBCProtocol(final String dbType, final String protocol) {
        this.dbType = dbType;
        this.protocol = protocol;
    }
    
    /**
     * get dbType.
     *
     * @return string. db type
     */
    public String getDbType() {
        return dbType;
    }
    
    /**
     * Sets db type.
     *
     * @param dbType the db type
     */
    public void setDbType(final String dbType) {
        this.dbType = dbType;
    }
    
    /**
     * Gets protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }
    
    /**
     * Sets protocol.
     *
     * @param protocol the protocol
     */
    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }
    
    /**
     * 根据jdbc url查询当前的dbtype.
     *
     * @param url url
     * @return JDBCProtocol
     */
    public static JDBCProtocol getProtocol(String url) {
        Optional<JDBCProtocol> first = Arrays.stream(JDBCProtocol.values()).filter(e -> url.startsWith(e.protocol)).findFirst();
        return first.orElse(null);
    }
}
