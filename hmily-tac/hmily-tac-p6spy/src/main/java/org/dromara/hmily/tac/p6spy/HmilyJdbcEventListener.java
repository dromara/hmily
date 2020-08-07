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

package org.dromara.hmily.tac.p6spy;

import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.common.PreparedStatementInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.JdbcEventListener;
import java.sql.SQLException;

/**
 * The type Hmily jdbc event listener.
 *
 * @author xiaoyu
 */
public class HmilyJdbcEventListener extends JdbcEventListener {
    @Override
    public void onAfterExecuteUpdate(final PreparedStatementInformation statementInformation, final long timeElapsedNanos, final int rowCount, final SQLException e) {
        super.onAfterExecuteUpdate(statementInformation, timeElapsedNanos, rowCount, e);
    }
    
    @Override
    public void onAfterExecuteUpdate(final StatementInformation statementInformation, final long timeElapsedNanos, final String sql, final int rowCount, final SQLException e) {
        super.onAfterExecuteUpdate(statementInformation, timeElapsedNanos, sql, rowCount, e);
    }
    
    @Override
    public void onAfterCommit(final ConnectionInformation connectionInformation, final long timeElapsedNanos, final SQLException e) {
        super.onAfterCommit(connectionInformation, timeElapsedNanos, e);
    }
    
    @Override
    public void onAfterRollback(final ConnectionInformation connectionInformation, final long timeElapsedNanos, final SQLException e) {
        super.onAfterRollback(connectionInformation, timeElapsedNanos, e);
    }
    
    @Override
    public void onAfterSetAutoCommit(final ConnectionInformation connectionInformation, final boolean newAutoCommit, final boolean oldAutoCommit, final SQLException e) {
        super.onAfterSetAutoCommit(connectionInformation, newAutoCommit, oldAutoCommit, e);
    }
}

