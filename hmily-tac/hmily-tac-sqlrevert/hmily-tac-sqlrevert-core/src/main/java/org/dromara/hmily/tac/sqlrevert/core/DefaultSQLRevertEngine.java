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

package org.dromara.hmily.tac.sqlrevert.core;

import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyUndoInvocation;
import org.dromara.hmily.spi.HmilySPI;
import org.dromara.hmily.tac.common.HmilyResourceManager;
import org.dromara.hmily.tac.sqlrevert.spi.HmilySQLRevertEngine;
import org.dromara.hmily.tac.sqlrevert.spi.exception.SQLRevertException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * The type Default SQL revert engine.
 *
 * @author xiaoyu
 * @author zhaojun
 */
@HmilySPI("default")
@Slf4j
public class DefaultSQLRevertEngine implements HmilySQLRevertEngine {
    
    @Override
    public boolean revert(final HmilyParticipantUndo participantUndo) throws SQLRevertException {
        String revertSQL = generateRevertSQL(participantUndo.getUndoInvocation());
        DataSource dataSource = HmilyResourceManager.get(participantUndo.getResourceId()).getTargetDataSource();
        return executeUpdate(revertSQL, dataSource) > 0;
    }
    
    // TODO generate RevertSQLUnit (revert SQL and parameters) here, we need a RevertSQLGenerateFactory
    private String generateRevertSQL(final HmilyUndoInvocation undoInvocation) {
        String sql = undoInvocation.getOriginSql();
        String result;
        if (sql.contains("order")) {
            String number = sql.substring(sql.indexOf("'") + 1, sql.length() - 1);
            result = "update `order` set status = 3 where number = " + number;
        } else if (sql.contains("account")) {
            result = "update account set balance = balance + 1  where user_id = 10000 ";
        } else {
            result = "update inventory set total_inventory = total_inventory + 1 where product_id = 1";
        }
        return result;
    }
    
    private int executeUpdate(final String sql, final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            log.error("hmily tac rollback exception -> ", e);
            return 0;
        }
    }
}
