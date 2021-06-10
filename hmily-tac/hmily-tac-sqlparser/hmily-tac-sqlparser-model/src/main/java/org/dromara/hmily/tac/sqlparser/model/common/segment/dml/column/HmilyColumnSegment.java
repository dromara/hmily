/*
 * Copyright 2017-2021 Dromara.org

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

package org.dromara.hmily.tac.sqlparser.model.common.segment.dml.column;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.HmilyExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.HmilyOwnerAvailable;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.HmilyOwnerSegment;
import org.dromara.hmily.tac.sqlparser.model.common.value.identifier.HmilyIdentifierValue;

import java.util.Optional;

/**
 * Column segment.
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public final class HmilyColumnSegment implements HmilyExpressionSegment, HmilyOwnerAvailable {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final HmilyIdentifierValue identifier;
    
    private HmilyOwnerSegment owner;
    
    /**
     * Get qualified name with quote characters.
     * i.e. `field1`, `table1`, field1, table1, `table1`.`field1`, `table1`.field1, table1.`field1` or table1.field1
     *
     * @return qualified name with quote characters
     */
    public String getQualifiedName() {
        return null == owner
                ? identifier.getValueWithQuoteCharacters()
                : String.join(".", owner.getIdentifier().getValueWithQuoteCharacters(), identifier.getValueWithQuoteCharacters());
    }
    
    @Override
    public Optional<HmilyOwnerSegment> getOwner() {
        return Optional.ofNullable(owner);
    }
}
