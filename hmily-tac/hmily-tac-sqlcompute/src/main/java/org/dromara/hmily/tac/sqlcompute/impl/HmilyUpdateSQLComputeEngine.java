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

package org.dromara.hmily.tac.sqlcompute.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.hmily.repository.spi.entity.HmilySQLTuple;
import org.dromara.hmily.repository.spi.entity.HmilyUndoInvocation;
import org.dromara.hmily.tac.sqlcompute.exception.SQLComputeException;
import org.dromara.hmily.tac.sqlparser.model.statement.dml.HmilyUpdateStatement;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hmily update SQL compute engine.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class HmilyUpdateSQLComputeEngine extends AbstractHmilySQLComputeEngine {
    
    private final HmilyUpdateStatement statement;
    
    @Override
    // TODO fixture undoInvocation for poc test
    public HmilyUndoInvocation generateImage(final Connection connection, final String sql) throws SQLComputeException {
        Map<String, Object> beforeImage = new LinkedHashMap<>();
        Map<String, Object> afterImage = new LinkedHashMap<>();
        HmilyUndoInvocation result = new HmilyUndoInvocation();
        if (sql.contains("order")) {
            beforeImage.put("status", 3);
            afterImage.put("number", sql.substring(sql.indexOf("'") + 1, sql.length() - 1));
            result.getTuples().add(new HmilySQLTuple("order", "update", beforeImage, afterImage));
        } else if (sql.contains("account")) {
            beforeImage.put("balance", 100);
            afterImage.put("user_id", 10000);
            result.getTuples().add(new HmilySQLTuple("account", "update", beforeImage, afterImage));
        } else {
            beforeImage.put("total_inventory", 100);
            afterImage.put("product_id", 1);
            result.getTuples().add(new HmilySQLTuple("inventory", "update", beforeImage, afterImage));
        }
        return result;
    }
    
    @Override
    List<ImageSQLUnit> generateQueryImageSQLs(final String originalSQL) {
        // TODO generate image SQL group according to parsed statement
        return null;
    }
}
