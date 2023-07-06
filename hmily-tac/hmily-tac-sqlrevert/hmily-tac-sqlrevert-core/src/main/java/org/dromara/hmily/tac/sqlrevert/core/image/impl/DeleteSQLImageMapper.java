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

package org.dromara.hmily.tac.sqlrevert.core.image.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.hmily.tac.sqlrevert.core.image.CreateSQLUtil;
import org.dromara.hmily.tac.sqlrevert.core.image.RevertSQLUnit;
import org.dromara.hmily.tac.sqlrevert.core.image.SQLImageMapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Delete SQL image mapper.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class DeleteSQLImageMapper implements SQLImageMapper {
    
    private final String tableName;
    
    private final Map<String, Object> beforeImages;
    
    @Override
    public RevertSQLUnit cast() {
        String sql = String.format("INSERT `%s` %s",
                tableName, CreateSQLUtil.getInsertValuesClause(beforeImages.keySet()));
        List<Object> parameters = new LinkedList<>(beforeImages.values());
        return new RevertSQLUnit(sql, parameters);
    }
}
