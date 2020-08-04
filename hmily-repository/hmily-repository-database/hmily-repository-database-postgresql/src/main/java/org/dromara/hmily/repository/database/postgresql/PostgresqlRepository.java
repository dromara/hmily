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

package org.dromara.hmily.repository.database.postgresql;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.repository.database.manager.AbstractHmilyDatabase;
import org.dromara.hmily.spi.HmilySPI;

/**
 * The type Postgresql repository.
 *
 * @author xiaoyu
 */
@HmilySPI(value = "postgresql")
@Slf4j
public class PostgresqlRepository extends AbstractHmilyDatabase {
    
    @Override
    protected String sqlFilePath() {
        return "postgresql/schema.sql";
    }
    
    @Override
    protected String hmilyTransactionLimitSql(final int limit) {
        return null;
    }
    
    @Override
    protected String hmilyParticipantLimitSql(final int limit) {
        return null;
    }
    
    @Override
    protected Object convertDataType(final Object params) {
        //https://jdbc.postgresql.org/documentation/head/8-date-time.html
        if (params instanceof java.util.Date) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(((Date) params).getTime()), ZoneId.systemDefault());
        }
        return params;
    }
}
