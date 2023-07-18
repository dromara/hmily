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

package org.dromara.hmily.tac.sqlparser.shardingsphere.common.handler;

import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.tac.sqlparser.model.common.constant.HmilyOrderDirection;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.column.HmilyColumnSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.HmilyExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.order.HmilyOrderBySegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.order.item.HmilyColumnOrderByItemSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.order.item.HmilyExpressionOrderByItemSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.order.item.HmilyIndexOrderByItemSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.order.item.HmilyOrderByItemSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.predicate.HmilyWhereSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.table.HmilySimpleTableSegment;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilySelectStatement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class SelectStatementAssembler {

    /**
     * Assemble Hmily select statement.
     *
     * @param selectStatement      select statement
     * @param hmilySelectStatement hmily select statement
     * @return hmily select statement
     */
    public static HmilySelectStatement assembleHmilySelectStatement(final SelectStatement selectStatement, final HmilySelectStatement hmilySelectStatement) {
        HmilySimpleTableSegment hmilySimpleTableSegment = CommonAssembler.assembleHmilySimpleTableSegment((SimpleTableSegment) selectStatement.getFrom());
        HmilyWhereSegment hmilyWhereSegment = null;
        if (selectStatement.getWhere().isPresent()) {
            hmilyWhereSegment = assembleHmilyWhereSegment(selectStatement.getWhere().get());
        }
        hmilySelectStatement.setTableSegment(hmilySimpleTableSegment);
        hmilySelectStatement.setWhere(hmilyWhereSegment);

        HmilyOrderBySegment hmilyOrderBySegment = null;
        if (selectStatement.getOrderBy().isPresent()) {
            hmilyOrderBySegment = assembleHmilyOrderBySegment(selectStatement.getOrderBy().get());
        }
        hmilySelectStatement.setOrderBy(hmilyOrderBySegment);
        return hmilySelectStatement;
    }

    private static HmilyWhereSegment assembleHmilyWhereSegment(final WhereSegment whereSegment) {
        HmilyExpressionSegment hmilyExpressionSegment = CommonAssembler.assembleHmilyExpressionSegment(whereSegment.getExpr());
        return new HmilyWhereSegment(whereSegment.getStartIndex(), whereSegment.getStopIndex(), hmilyExpressionSegment);
    }

    private static HmilyOrderBySegment assembleHmilyOrderBySegment(final OrderBySegment orderBySegment) {
        Collection<OrderByItemSegment> orderByItems = orderBySegment.getOrderByItems();
        List<HmilyOrderByItemSegment> hmilyOrderByItemSegments = null;
        if (CollectionUtils.isNotEmpty(orderByItems)) {
            hmilyOrderByItemSegments = orderByItems.stream().map(orderByItemSegment -> {
                HmilyOrderByItemSegment hmilyOrderByItemSegment = null;
                int startIndex = orderByItemSegment.getStartIndex();
                int stopIndex = orderByItemSegment.getStopIndex();
                HmilyOrderDirection hmilyOrderDirection = null;
                HmilyOrderDirection nullHmilyOrderDirection = null;
                OrderDirection orderDirection = orderByItemSegment.getOrderDirection();
                if (orderDirection.equals(OrderDirection.ASC)) {
                    hmilyOrderDirection = HmilyOrderDirection.ASC;
                } else {
                    hmilyOrderDirection = HmilyOrderDirection.DESC;
                }
                OrderDirection nullOrderDirection = orderByItemSegment.getNullOrderDirection();
                if (nullOrderDirection.equals(OrderDirection.ASC)) {
                    nullHmilyOrderDirection = HmilyOrderDirection.ASC;
                } else {
                    nullHmilyOrderDirection = HmilyOrderDirection.DESC;
                }
                if (orderByItemSegment instanceof IndexOrderByItemSegment) {
                    hmilyOrderByItemSegment = new HmilyIndexOrderByItemSegment(startIndex, stopIndex,
                            ((IndexOrderByItemSegment) orderByItemSegment).getColumnIndex(), hmilyOrderDirection, nullHmilyOrderDirection);
                } else if (orderByItemSegment instanceof ColumnOrderByItemSegment) {
                    ColumnSegment columnSegment = ((ColumnOrderByItemSegment) orderByItemSegment).getColumn();
                    HmilyColumnSegment hmilyColumnSegment = CommonAssembler.assembleHmilyColumnSegment(columnSegment);
                    hmilyOrderByItemSegment = new HmilyColumnOrderByItemSegment(hmilyColumnSegment, hmilyOrderDirection, nullHmilyOrderDirection);
                } else if (orderByItemSegment instanceof ExpressionOrderByItemSegment) {
                    ExpressionOrderByItemSegment expressionOrderByItemSegment = (ExpressionOrderByItemSegment) orderByItemSegment;
                    hmilyOrderByItemSegment = new HmilyExpressionOrderByItemSegment(startIndex, stopIndex, expressionOrderByItemSegment.getExpression(),
                            hmilyOrderDirection, nullHmilyOrderDirection);
                }
                return hmilyOrderByItemSegment;
            }).collect(Collectors.toList());
        }
        return new HmilyOrderBySegment(orderBySegment.getStartIndex(), orderBySegment.getStopIndex(), hmilyOrderByItemSegments);
    }
}
