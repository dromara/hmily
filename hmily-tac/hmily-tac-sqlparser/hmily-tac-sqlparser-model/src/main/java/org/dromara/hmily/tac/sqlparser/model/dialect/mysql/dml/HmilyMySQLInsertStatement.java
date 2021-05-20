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

package org.dromara.hmily.tac.sqlparser.model.dialect.mysql.dml;

import lombok.Setter;
import lombok.ToString;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.assignment.HmilySetAssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.column.HmilyOnDuplicateKeyColumnsSegment;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilyInsertStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.mysql.HmilyMySQLStatement;

import java.util.Optional;

/**
 * MySQL insert statement.
 */
@Setter
@ToString
public final class HmilyMySQLInsertStatement extends HmilyInsertStatement implements HmilyMySQLStatement {

    private HmilySetAssignmentSegment setAssignment;

    private HmilyOnDuplicateKeyColumnsSegment onDuplicateKeyColumns;

    /**
     * Get set assignment segment.
     *
     * @return set assignment segment
     */
    public Optional<HmilySetAssignmentSegment> getSetAssignment() {
        return Optional.ofNullable(setAssignment);
    }

    /**
     * Get on duplicate key columns segment.
     *
     * @return on duplicate key columns segment
     */
    public Optional<HmilyOnDuplicateKeyColumnsSegment> getOnDuplicateKeyColumns() {
        return Optional.ofNullable(onDuplicateKeyColumns);
    }
}
