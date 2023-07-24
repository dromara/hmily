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

package org.dromara.hmily.tac.sqlparser.shardingsphere;

import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngineFactory;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionBuilder;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.dromara.hmily.tac.common.constants.DatabaseConstant;
import org.dromara.hmily.tac.common.database.type.MySQLDatabaseType;
import org.dromara.hmily.tac.sqlparser.model.common.statement.HmilyStatement;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ShardingSphereSqlParserEngineTest {
    
    @Test
    public void assertUpdateWhereAndOr() {
        String sql = "update t_order set amount=amount-? where order_id=? and user_id=? and status=? or retry=?";
        MySQLUpdateStatement statement = (MySQLUpdateStatement) SQLStatementParserEngineFactory.getSQLStatementParserEngine(DatabaseConstant.MYSQL).parse(sql, true);
        assertTrue(statement.getWhere().isPresent());
        OrPredicateSegment orPredicateSegment = new ExpressionBuilder(statement.getWhere().get().getExpr()).extractAndPredicates();
        System.out.println(orPredicateSegment);
    }
    
    @Test
    public void assertUpdateWhereBetween() {
        String sql = "update t_order set amount=amount-? where order_id in (?,?,?) and id =? or retry=?";
        MySQLUpdateStatement statement = (MySQLUpdateStatement) SQLStatementParserEngineFactory.getSQLStatementParserEngine(DatabaseConstant.MYSQL).parse(sql, true);
        assertTrue(statement.getWhere().isPresent());
        OrPredicateSegment orPredicateSegment = new ExpressionBuilder(statement.getWhere().get().getExpr()).extractAndPredicates();
        System.out.println(orPredicateSegment);
    }
    
    @Test
    public void assertHmilyUpdateWhere() {
        String sql = "update t_order set amount=amount-?, count=count-1, xx=? where id =? and retry=?";
        ShardingSphereSqlParserEngine shardingSphereSqlParserEngine = new ShardingSphereSqlParserEngine();
        HmilyStatement hmilyStatement = shardingSphereSqlParserEngine.parser(sql, new MySQLDatabaseType());
        System.out.println(hmilyStatement);
    }

    @Test
    public void assertHmilySelectWhere() {
        String sql = "select * from t_order where id =? and retry=? order by update_time ASC limit 1";
        ShardingSphereSqlParserEngine shardingSphereSqlParserEngine = new ShardingSphereSqlParserEngine();
        HmilyStatement hmilyStatement = shardingSphereSqlParserEngine.parser(sql, new MySQLDatabaseType());
        System.out.println(hmilyStatement);
    }
}
