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

package org.dromara.hmily.xa.p6spy.mysql;

/**
 * Mysql8WarpXaConnection .
 *
 * @author sixh chenbin
 */
public class Mysql8WarpXaConnection extends Mysql6WarpXaConnection {
    @Override
    protected String getConnectionClassName() {
        return "com.mysql.cj.jdbc.JdbcConnection";
    }
    
    @Override
    protected String getPropertySetClass() {
        return "com.mysql.cj.conf.PropertySet";
    }
    
    @Override
    protected String getReadablePropertyClassName() {
        return "com.mysql.cj.conf.ReadableProperty";
    }
    
    @Override
    protected int getVersion() {
        return 8;
    }
}
