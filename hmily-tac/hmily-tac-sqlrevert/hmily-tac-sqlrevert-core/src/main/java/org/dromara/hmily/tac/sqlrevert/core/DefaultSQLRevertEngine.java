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
import org.dromara.hmily.spi.HmilySPI;
import org.dromara.hmily.tac.common.HmilyResourceManager;
import org.dromara.hmily.tac.sqlrevert.core.image.RevertSQLUnit;
import org.dromara.hmily.tac.sqlrevert.core.image.SQLImageMapperFactory;
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
        RevertSQLUnit revertSQLUnit = SQLImageMapperFactory.newInstance(participantUndo.getUndoInvocation()).cast();
        DataSource dataSource = HmilyResourceManager.get(participantUndo.getResourceId()).getTargetDataSource();
        return executeUpdate(revertSQLUnit, dataSource) > 0;
    }
    
    private int executeUpdate(final RevertSQLUnit unit, final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(unit.getSql())) {
            int index = 1;
            for (Object each : unit.getParameters()) {
                preparedStatement.setObject(index, each);
                index++;
            }
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("hmily tac rollback exception -> ", e);
            return 0;
        }
    }
}
