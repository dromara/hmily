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

package org.dromara.hmily.tac.sqlparser.model.segment.dml.item;

import lombok.Getter;
import lombok.Setter;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.complex.HmilyComplexExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.HmilyAliasAvailable;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.HmilyAliasSegment;
import org.dromara.hmily.tac.sqlparser.model.util.HmilySQLUtil;

import java.util.Optional;

/**
 * Expression projection segment.
 */
@Getter
public final class HmilyExpressionProjectionSegment implements HmilyProjectionSegment, HmilyComplexExpressionSegment, HmilyAliasAvailable {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final String text;
    
    @Setter
    private HmilyAliasSegment alias;
    
    public HmilyExpressionProjectionSegment(final int startIndex, final int stopIndex, final String text) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.text = HmilySQLUtil.getExpressionWithoutOutsideParentheses(text);
    }
    
    @Override
    public Optional<String> getAlias() {
        return null == alias ? Optional.empty() : Optional.ofNullable(alias.getIdentifier().getValue());
    }
}
