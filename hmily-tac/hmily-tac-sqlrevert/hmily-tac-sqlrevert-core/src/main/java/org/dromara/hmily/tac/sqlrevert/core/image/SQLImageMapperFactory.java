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

package org.dromara.hmily.tac.sqlrevert.core.image;

import org.dromara.hmily.repository.spi.entity.tuple.HmilySQLTuple;
import org.dromara.hmily.tac.sqlrevert.core.image.impl.DeleteSQLImageMapper;
import org.dromara.hmily.tac.sqlrevert.core.image.impl.InsertSQLImageMapper;
import org.dromara.hmily.tac.sqlrevert.core.image.impl.UpdateSQLImageMapper;
import org.dromara.hmily.tac.sqlrevert.spi.exception.SQLRevertException;

/**
 * SQL image mapper factory.
 *
 * @author zhaojun
 */
public class SQLImageMapperFactory {
    
    /**
     * Create new instance of SQL image mapper.
     *
     * @param sqlTuple SQL tuple
     * @return SQL image mapper
     */
    public static SQLImageMapper newInstance(final HmilySQLTuple sqlTuple) {
        switch (sqlTuple.getManipulationType()) {
            case INSERT:
                return new InsertSQLImageMapper(sqlTuple.getTableName(), sqlTuple.getAfterImage());
            case UPDATE:
                return new UpdateSQLImageMapper(sqlTuple.getTableName(), sqlTuple.getBeforeImage(), sqlTuple.getAfterImage());
            case DELETE:
                return new DeleteSQLImageMapper(sqlTuple.getTableName(), sqlTuple.getBeforeImage());
            default:
                throw new SQLRevertException(String.format("unsupported SQL manipulate type [%s]", sqlTuple.getManipulationType()));
        }
    }
}
