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

package org.dromara.hmily.tac.sqlparser.model.value.identifier;

import lombok.Getter;
import org.dromara.hmily.tac.sqlparser.model.constant.HmilyQuoteCharacter;
import org.dromara.hmily.tac.sqlparser.model.util.HmilySQLUtil;
import org.dromara.hmily.tac.sqlparser.model.value.HmilyValueASTNode;

/**
 * Identifier value.
 */
@Getter
public final class HmilyIdentifierValue implements HmilyValueASTNode<String> {
    
    private final String value;
    
    private final HmilyQuoteCharacter hmilyQuoteCharacter;
    
    public HmilyIdentifierValue(final String text) {
        value = HmilySQLUtil.getExactlyValue(text);
        hmilyQuoteCharacter = HmilyQuoteCharacter.getQuoteCharacter(text);
    }
    
    public HmilyIdentifierValue(final String value, final HmilyQuoteCharacter hmilyQuoteCharacter) {
        this.value = value;
        this.hmilyQuoteCharacter = hmilyQuoteCharacter;
    }
    
    @Override
    public String toString() {
        return hmilyQuoteCharacter.getStartDelimiter() + value + hmilyQuoteCharacter.getEndDelimiter();
    }
}
