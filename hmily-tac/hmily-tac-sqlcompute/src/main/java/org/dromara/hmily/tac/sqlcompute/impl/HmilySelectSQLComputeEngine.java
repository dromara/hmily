package org.dromara.hmily.tac.sqlcompute.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.hmily.repository.spi.entity.tuple.HmilySQLManipulation;
import org.dromara.hmily.repository.spi.entity.tuple.HmilySQLTuple;
import org.dromara.hmily.tac.metadata.HmilyMetaDataManager;
import org.dromara.hmily.tac.metadata.model.TableMetaData;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.order.HmilyOrderBySegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.pagination.limit.HmilyLimitSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.table.HmilySimpleTableSegment;
import org.dromara.hmily.tac.sqlparser.model.dialect.mysql.dml.HmilyMySQLSelectStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hmily select SQL compute engine.
 *
 * @author zhangzhi
 */
@RequiredArgsConstructor
public class HmilySelectSQLComputeEngine extends AbstractHmilySQLComputeEngine {

    private final HmilyMySQLSelectStatement sqlStatement;

    @Override
    Collection<HmilySQLTuple> createTuples(final String sql, final List<Object> parameters, final Connection connection, final String resourceId) throws SQLException {
        Collection<HmilySQLTuple> result = new LinkedList<>();
        HmilySimpleTableSegment tableSegment = (HmilySimpleTableSegment) sqlStatement.getTableSegment();
        String tableName = tableSegment.getTableName().getIdentifier().getValue();
        List<String> primaryKeyColumns = HmilyMetaDataManager.get(resourceId).getTableMetaDataMap().get(tableName).getPrimaryKeyColumns();
        String selectPKSQL = String.format("SELECT %s FROM %s %s %s %s", HmilySQLComputeUtils.getAllPKColumns(tableSegment, tableName, primaryKeyColumns), tableName,
                getWhereCondition(sql), getOrderByCondition(sql), getLimitCondition(sql));
        Collection<Map<String, Object>> records = HmilySQLComputeUtils.executeQuery(connection, selectPKSQL, parameters);
        result.addAll(doConvert(records, HmilyMetaDataManager.get(resourceId).getTableMetaDataMap().get(tableName)));
        return result;
    }

    private String getWhereCondition(final String sql) {
        return sqlStatement.getWhere().map(segment -> sql.substring(segment.getStartIndex(), segment.getStopIndex() + 1)).orElse("");
    }

    private String getOrderByCondition(final String sql) {
        HmilyOrderBySegment orderBy = sqlStatement.getOrderBy();
        if (orderBy != null) {
            return sql.substring(orderBy.getStartIndex(), orderBy.getStopIndex() + 1);
        }
        return "";
    }

    private String getLimitCondition(final String sql) {
        if (sqlStatement.getLimit().isPresent()) {
            HmilyLimitSegment limitSegment = sqlStatement.getLimit().get();
            return sql.substring(limitSegment.getStartIndex(), limitSegment.getStopIndex() + 1);
        }
        return "";
    }

    private Collection<HmilySQLTuple> doConvert(final Collection<Map<String, Object>> records, final TableMetaData tableMetaData) {
        Collection<HmilySQLTuple> result = new LinkedList<>();
        for (Map<String, Object> record : records) {
            List<Object> primaryKeyValues = tableMetaData.getPrimaryKeyColumns().stream().map(record::get).collect(Collectors.toList());
            result.add(buildTuple(tableMetaData.getTableName(), HmilySQLManipulation.SELECT, primaryKeyValues, new LinkedHashMap<>(), new LinkedHashMap<>()));
        }
        return result;
    }
}
