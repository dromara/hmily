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

package com.hmily.tcc.common.utils;


import com.hmily.tcc.common.constant.CommonConstant;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/20 11:39
 * @since JDK 1.8
 */
public class DbTypeUtils {

    public static String buildByDriverClassName(String driverClassName) {
        String dbType = "mysql";
        if (driverClassName.contains(CommonConstant.DB_MYSQL)) {
            dbType = "mysql";
        } else if (driverClassName.contains(CommonConstant.DB_SQLSERVER)) {
            dbType = "sqlserver";
        } else if (driverClassName.contains(CommonConstant.DB_ORACLE)) {
            dbType = "oracle";
        }
        return dbType;
    }


}
