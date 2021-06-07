/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.tac.sqlparser.model.common.statement.dml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.predicate.HmilyWhereSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.table.HmilyTableSegment;
import org.dromara.hmily.tac.sqlparser.model.common.statement.AbstractHmilyStatement;

import java.util.Optional;

/**
 * Delete statement.
 */
@Setter
@ToString
public abstract class HmilyDeleteStatement extends AbstractHmilyStatement implements HmilyDMLStatement {
    
    @Getter
    private HmilyTableSegment tableSegment;
    
    private HmilyWhereSegment where;
    
    /**
     * Get where.
     *
     * @return where segment
     */
    public Optional<HmilyWhereSegment> getWhere() {
        return Optional.ofNullable(where);
    }
}
