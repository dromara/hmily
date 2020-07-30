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

package org.dromara.hmily.repository.database.oracle;

import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.repository.database.manager.AbstractHmilyDatabase;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.spi.HmilySPI;

/**
 * The type Oracle repository.
 *
 * @author xiaoyu
 */
@HmilySPI(value = "oracle")
@Slf4j
public class OracleRepository extends AbstractHmilyDatabase {
    
    @Override
    protected String sqlFilePath() {
        return "oracle/schema.sql";
    }
    
    @Override
    protected String hmilyTransactionLimitSql() {
        return null;
    }
    
    @Override
    protected String hmilyParticipantLimitSql() {
        return null;
    }
    
    @Override
    protected Object convertDataType(final Object params) {
        return params;
    }
}
