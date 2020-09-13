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

package org.dromara.hmily.tac.p6spy.listener;

import com.p6spy.engine.common.CallableStatementInformation;
import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.common.PreparedStatementInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.JdbcEventListener;
import java.sql.SQLException;
import java.util.Objects;
import org.dromara.hmily.tac.p6spy.executor.HmilyExecuteTemplate;

/**
 * The type Hmily jdbc event listener.
 *
 * @author xiaoyu
 */
public class HmilyJdbcEventListener extends JdbcEventListener {
    
    @Override
    public void onAfterGetConnection(final ConnectionInformation connectionInformation, final SQLException e) {
        super.onAfterGetConnection(connectionInformation, e);
        if (Objects.isNull(e)) {
            HmilyExecuteTemplate.INSTANCE.beforeSetAutoCommit(connectionInformation.getConnection());
        }
    }
    
    @Override
    public void onBeforeExecute(final PreparedStatementInformation statementInformation) {
        super.onBeforeExecute(statementInformation);
        HmilyExecuteTemplate.INSTANCE.execute(statementInformation);
    }
    
    @Override
    public void onBeforeExecute(final StatementInformation statementInformation, final String sql) {
        super.onBeforeExecute(statementInformation, sql);
        HmilyExecuteTemplate.INSTANCE.execute(statementInformation);
    }
    
    @Override
    public void onBeforeExecuteUpdate(final PreparedStatementInformation statementInformation) {
        super.onBeforeExecuteUpdate(statementInformation);
        HmilyExecuteTemplate.INSTANCE.execute(statementInformation);
    }
    
    @Override
    public void onBeforeExecuteUpdate(final StatementInformation statementInformation, final String sql) {
        super.onBeforeExecuteUpdate(statementInformation, sql);
        HmilyExecuteTemplate.INSTANCE.execute(statementInformation);
    }
    
    @Override
    public void onAfterCommit(final ConnectionInformation connectionInformation, final long timeElapsedNanos, final SQLException e) {
        super.onAfterCommit(connectionInformation, timeElapsedNanos, e);
        if (Objects.isNull(e)) {
            HmilyExecuteTemplate.INSTANCE.commit(connectionInformation.getConnection());
        } else {
            HmilyExecuteTemplate.INSTANCE.clean(connectionInformation.getConnection());
        }
    }
    
    @Override
    public void onAfterRollback(final ConnectionInformation connectionInformation, final long timeElapsedNanos, final SQLException e) {
        super.onAfterRollback(connectionInformation, timeElapsedNanos, e);
        HmilyExecuteTemplate.INSTANCE.clean(connectionInformation.getConnection());
    }
    
    @Override
    public void onAfterSetAutoCommit(final ConnectionInformation connectionInformation, final boolean newAutoCommit, final boolean oldAutoCommit, final SQLException e) {
        super.onAfterSetAutoCommit(connectionInformation, newAutoCommit, oldAutoCommit, e);
    }
    
    @Override
    public void onAfterCallableStatementSet(final CallableStatementInformation statementInformation, final String parameterName, final Object value, final SQLException e) {
        statementInformation.setParameterValue(parameterName, value);
    }
    
    @Override
    public void onAfterPreparedStatementSet(final PreparedStatementInformation statementInformation, final int parameterIndex, final Object value, final SQLException e) {
        statementInformation.setParameterValue(parameterIndex, value);
    }
}

