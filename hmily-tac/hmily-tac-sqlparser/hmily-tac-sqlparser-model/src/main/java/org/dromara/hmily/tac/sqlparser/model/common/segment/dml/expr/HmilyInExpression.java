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

package org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedList;

@Getter
@Setter
@RequiredArgsConstructor
public final class HmilyInExpression implements HmilyExpressionSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final HmilyExpressionSegment left;
    
    private final HmilyExpressionSegment right;
    
    private final boolean not;
    
    /**
     * Get expression list from right.
     *
     * @return expression list.
     */
    public Collection<HmilyExpressionSegment> getExpressionList() {
        Collection<HmilyExpressionSegment> result = new LinkedList<>();
        if (right instanceof HmilyListExpression) {
            result.addAll(((HmilyListExpression) right).getItems());
        } else {
            result.add(this);
        }
        return result;
    }
}
