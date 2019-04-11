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

package org.dromara.hmily.common.constant;

/**
 * CommonConstant.
 *
 * @author xiaoyu
 */
public final class CommonConstant {

    private CommonConstant() {
    }

    /**
     * The constant DB_MYSQL.
     */
    public final static String DB_MYSQL = "mysql";

    /**
     * The constant DB_SQLSERVER.
     */
    public final static String DB_SQLSERVER = "sqlserver";

    /**
     * The constant DB_ORACLE.
     */
    public final static String DB_ORACLE = "oracle";

    /**
     * The constant DB_POSTGRESQL.
     */
    public final static String DB_POSTGRESQL = "postgresql";

    /**
     * The constant PATH_SUFFIX.
     */
    public final static String PATH_SUFFIX = "/tcc";

    /**
     * The constant DB_SUFFIX.
     */
    public final static String DB_SUFFIX = "hmily_";

    /**
     * The constant RECOVER_REDIS_KEY_PRE.
     */
    public final static String RECOVER_REDIS_KEY_PRE = "hmily:transaction:%s";

    /**
     * The constant HMILY_TRANSACTION_CONTEXT.
     */
    public final static String HMILY_TRANSACTION_CONTEXT = "HMILY_TRANSACTION_CONTEXT";

    /**
     * The constant LINE_SEPARATOR.
     */
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");

}
