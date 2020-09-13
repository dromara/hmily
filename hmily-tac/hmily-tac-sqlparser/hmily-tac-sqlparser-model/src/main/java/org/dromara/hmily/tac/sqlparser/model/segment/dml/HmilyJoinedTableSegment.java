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

package org.dromara.hmily.tac.sqlparser.model.segment.dml;

import lombok.Getter;
import lombok.Setter;
import org.dromara.hmily.tac.sqlparser.model.segment.HmilySegment;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.table.HmilySimpleTableSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.table.HmilyTableSegment;

@Getter
@Setter
public final class HmilyJoinedTableSegment implements HmilySegment {
    
    private int startIndex;
    
    private int stopIndex;
    
    private HmilyTableFactorSegment tableFactor;
    
    private HmilyJoinSpecificationSegment joinSpecification;
    
    /**
     * get table.
     * @return tableSegment.
     */
    public HmilyTableSegment getTable() {
        if (null != tableFactor.getTable()) {
            if (tableFactor.getTable() instanceof HmilySimpleTableSegment) {
                return tableFactor.getTable();
            }
        }
        return null;
    }
}
