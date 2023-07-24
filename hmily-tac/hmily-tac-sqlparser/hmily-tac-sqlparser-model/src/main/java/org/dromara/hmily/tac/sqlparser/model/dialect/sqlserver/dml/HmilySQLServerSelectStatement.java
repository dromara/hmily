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

package org.dromara.hmily.tac.sqlparser.model.dialect.sqlserver.dml;

import lombok.Setter;
import lombok.ToString;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.pagination.limit.HmilyLimitSegment;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilySelectStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.sqlserver.HmilySQLServerStatement;

import java.util.Optional;

/**
 * SQLServer select statement.
 */
@Setter
@ToString
public final class HmilySQLServerSelectStatement extends HmilySelectStatement implements HmilySQLServerStatement {

    private HmilyLimitSegment limit;

    /**
     * Get limit segment.
     *
     * @return limit segment
     */
    public Optional<HmilyLimitSegment> getLimit() {
        return Optional.ofNullable(limit);
    }
}
