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

import lombok.RequiredArgsConstructor;
import org.dromara.hmily.tac.sqlparser.model.common.constant.HmilyLogicalOperator;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.HmilyBinaryOperationExpression;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.HmilyExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.predicate.HmilyAndPredicate;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.predicate.HmilyOrPredicateSegment;

import java.util.Optional;

/**
 * Expression builder.
 */
@RequiredArgsConstructor
public final class HmilyExpressionBuilder {
    
    private final HmilyExpressionSegment expression;
    
    /**
     * Extract and predicates.
     *
     * @return Or predicate segment.
     */
    public HmilyOrPredicateSegment extractAndPredicates() {
        HmilyOrPredicateSegment result = new HmilyOrPredicateSegment();
        if (expression instanceof HmilyBinaryOperationExpression) {
            String operator = ((HmilyBinaryOperationExpression) expression).getOperator();
            Optional<HmilyLogicalOperator> logicalOperator = HmilyLogicalOperator.valueFrom(operator);
            if (logicalOperator.isPresent() && HmilyLogicalOperator.OR == logicalOperator.get()) {
                HmilyExpressionBuilder leftBuilder = new HmilyExpressionBuilder(((HmilyBinaryOperationExpression) expression).getLeft());
                HmilyExpressionBuilder rightBuilder = new HmilyExpressionBuilder(((HmilyBinaryOperationExpression) expression).getRight());
                result.getHmilyAndPredicates().addAll(leftBuilder.extractAndPredicates().getHmilyAndPredicates());
                result.getHmilyAndPredicates().addAll(rightBuilder.extractAndPredicates().getHmilyAndPredicates());
            } else if (logicalOperator.isPresent() && HmilyLogicalOperator.AND == logicalOperator.get()) {
                HmilyExpressionBuilder leftBuilder = new HmilyExpressionBuilder(((HmilyBinaryOperationExpression) expression).getLeft());
                HmilyExpressionBuilder rightBuilder = new HmilyExpressionBuilder(((HmilyBinaryOperationExpression) expression).getRight());
                for (HmilyAndPredicate eachLeft : leftBuilder.extractAndPredicates().getHmilyAndPredicates()) {
                    for (HmilyAndPredicate eachRight : rightBuilder.extractAndPredicates().getHmilyAndPredicates()) {
                        result.getHmilyAndPredicates().add(createAndPredicate(eachLeft, eachRight));
                    }
                }
            } else {
                HmilyAndPredicate andPredicate = new HmilyAndPredicate();
                andPredicate.getPredicates().add(expression);
                result.getHmilyAndPredicates().add(andPredicate);
            }
        } else {
            HmilyAndPredicate andPredicate = new HmilyAndPredicate();
            andPredicate.getPredicates().add(expression);
            result.getHmilyAndPredicates().add(andPredicate);
        }
        return result;
    }
    
    private HmilyAndPredicate createAndPredicate(final HmilyAndPredicate left, final HmilyAndPredicate right) {
        HmilyAndPredicate result = new HmilyAndPredicate();
        result.getPredicates().addAll(left.getPredicates());
        result.getPredicates().addAll(right.getPredicates());
        return result;
    }
}
