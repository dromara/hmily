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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.HmilyExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.complex.HmilyCommonExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.simple.HmilyLiteralExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.simple.HmilyParameterMarkerExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.item.HmilyExpressionProjectionSegment;

import java.util.List;

/**
 * Expression utility.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionHandler {
    
    /**
     * Get expression value.
     * 
     * @param parameters SQL parameters
     * @param expressionSegment expression segment
     * @return expression value
     */
    public static Object getValue(final List<Object> parameters, final HmilyExpressionSegment expressionSegment) {
        if (expressionSegment instanceof HmilyCommonExpressionSegment) {
            String value = ((HmilyCommonExpressionSegment) expressionSegment).getText();
            //FIXME shardingsphere has bug with set `set balance = balance - ?`
            if (value.contains("?")) {
                return value.replace("?", parameters.get(0).toString());
            }
            return "null".equals(value) ? null : value;
        }
        if (expressionSegment instanceof HmilyParameterMarkerExpressionSegment) {
            return parameters.get(((HmilyParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
        }
        if (expressionSegment instanceof HmilyExpressionProjectionSegment) {
            String value = ((HmilyExpressionProjectionSegment) expressionSegment).getText();
            return "null".equals(value) ? null : value;
        }
        // TODO match result type with metadata
        return ((HmilyLiteralExpressionSegment) expressionSegment).getLiterals();
    }
}
