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

package org.dromara.hmily.tac.sqlparser.model.segment.dml.column;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dromara.hmily.tac.sqlparser.model.segment.SQLSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.predicate.value.PredicateRightValue;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.OwnerAvailable;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.OwnerSegment;
import org.dromara.hmily.tac.sqlparser.model.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Column segment.
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public final class ColumnSegment implements SQLSegment, PredicateRightValue, OwnerAvailable {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final IdentifierValue identifier;
    
    private OwnerSegment owner;
    
    /**
     * Get qualified name.
     *
     * @return qualified name
     */
    public String getQualifiedName() {
        return null == owner ? identifier.getValue() : owner.getIdentifier().getValue() + "." + identifier.getValue();
    }

    @Override
    public Optional<OwnerSegment> getOwner() {
        return Optional.ofNullable(owner);
    }
}
