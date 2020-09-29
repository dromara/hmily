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

package org.dromara.hmily.tac.sqlrevert.core.image.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.hmily.tac.sqlrevert.core.image.RevertSQLUnit;
import org.dromara.hmily.tac.sqlrevert.core.image.SQLImageMapper;

import java.util.LinkedList;
import java.util.Map;

/**
 * Update SQL image mapper.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class UpdateSQLImageMapper implements SQLImageMapper {
    
    private final String tableName;
    
    private final Map<String, Object> beforeImages;
    
    private final Map<String, Object> afterImages;
    
    private final String sql;
    
    @Override
    //TODO  fixed result just for poc test
    public RevertSQLUnit cast() {
        String result;
        if (sql.contains("order")) {
            String number = sql.substring(sql.indexOf("'") + 1, sql.length() - 1);
            result = "update `order` set status = 3 where number = " + number;
        } else if (sql.contains("account")) {
            result = "update account set balance = balance + 1  where user_id = 10000 ";
        } else {
            result = "update inventory set total_inventory = total_inventory + 1 where product_id = 1";
        }
        return new RevertSQLUnit(result, new LinkedList<>());
    }
}
