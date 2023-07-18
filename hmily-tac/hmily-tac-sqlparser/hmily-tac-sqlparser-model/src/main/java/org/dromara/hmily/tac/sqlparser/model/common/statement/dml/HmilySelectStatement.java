package org.dromara.hmily.tac.sqlparser.model.common.statement.dml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.order.HmilyOrderBySegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.predicate.HmilyWhereSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.table.HmilyTableSegment;
import org.dromara.hmily.tac.sqlparser.model.common.statement.AbstractHmilyStatement;

import java.util.Optional;

/**
 * select statement.
 *
 * @author zhangzhi
 */
@Getter
@Setter
@ToString
public abstract class HmilySelectStatement extends AbstractHmilyStatement implements HmilyDMLStatement {

    private HmilyTableSegment tableSegment;

    private HmilyWhereSegment where;

    private HmilyOrderBySegment orderBy;

    /**
     * Get where.
     *
     * @return where segment
     */
    public Optional<HmilyWhereSegment> getWhere() {
        return Optional.ofNullable(where);
    }

    /**
     * Get orderBy.
     *
     * @return orderBy segment
     */
    public Optional<HmilyOrderBySegment> gteOrderBy() {
        return Optional.ofNullable(orderBy);
    }
}
