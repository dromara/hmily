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

package org.dromara.hmily.tac.sqlparser.model.segment.generic.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.AliasAvailable;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.AliasSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.OwnerAvailable;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.OwnerSegment;
import org.dromara.hmily.tac.sqlparser.model.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Simple table segment.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class SimpleTableSegment implements TableSegment, OwnerAvailable, AliasAvailable {
    
    private final TableNameSegment tableName;
    
    @Setter
    private OwnerSegment owner;
    
    @Setter
    private AliasSegment alias;
    
    public SimpleTableSegment(final int startIndex, final int stopIndex, final IdentifierValue identifierValue) {
        tableName = new TableNameSegment(startIndex, stopIndex, identifierValue);
    }
    
    @Override
    public int getStartIndex() {
        return null == owner ? tableName.getStartIndex() : owner.getStartIndex(); 
    }
    
    @Override
    public int getStopIndex() {
        return tableName.getStopIndex();
        //FIXME: Rewriter need to handle alias as well
//        return null == alias ? tableName.getStopIndex() : alias.getStopIndex();
    }
    
    @Override
    public Optional<OwnerSegment> getOwner() {
        return Optional.ofNullable(owner);
    }
    
    @Override
    public Optional<String> getAlias() {
        return null == alias ? Optional.empty() : Optional.ofNullable(alias.getIdentifier().getValue());
    }
}
