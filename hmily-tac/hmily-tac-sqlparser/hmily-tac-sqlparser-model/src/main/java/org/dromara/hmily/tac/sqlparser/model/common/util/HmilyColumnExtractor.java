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

package org.dromara.hmily.tac.sqlparser.model.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.column.HmilyColumnSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.HmilyBetweenExpression;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.HmilyBinaryOperationExpression;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.HmilyExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.HmilyInExpression;

import java.util.Optional;

/**
 * Column extractor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HmilyColumnExtractor {
    
    /**
     * Get left value if left value of expression is column segment.
     *
     * @param expression expression segment
     * @return column segment
     */
    public static Optional<HmilyColumnSegment> extract(final HmilyExpressionSegment expression) {
        if (expression instanceof HmilyBinaryOperationExpression && ((HmilyBinaryOperationExpression) expression).getLeft() instanceof HmilyColumnSegment) {
            HmilyColumnSegment column = (HmilyColumnSegment) ((HmilyBinaryOperationExpression) expression).getLeft();
            return Optional.of(column);
        }
        if (expression instanceof HmilyInExpression && ((HmilyInExpression) expression).getLeft() instanceof HmilyColumnSegment) {
            HmilyColumnSegment column = (HmilyColumnSegment) ((HmilyInExpression) expression).getLeft();
            return Optional.of(column);
        }
        if (expression instanceof HmilyBetweenExpression && ((HmilyBetweenExpression) expression).getLeft() instanceof HmilyColumnSegment) {
            HmilyColumnSegment column = (HmilyColumnSegment) ((HmilyBetweenExpression) expression).getLeft();
            return Optional.of(column);
        }
        return Optional.empty();
    }
}
