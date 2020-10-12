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

package org.dromara.hmily.tac.sqlrevert.core.image;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * Create SQL util.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateSQLUtil {
    
    /**
     * Get insert values clause.
     *
     * @param keySet key set
     * @return insert values clause
     */
    public static String getInsertValuesClause(final Set<String> keySet) {
        Map<String, String> map = Maps.asMap(keySet, input -> "?");
        return String.format("(%s) VALUES (%s)", Joiner.on(",").join(map.keySet()), Joiner.on(",").join(map.values()));
    }
    
    /**
     * Get key value SQL clause.
     *
     * @param keySet key set
     * @param separator separator
     * @return key value SQL clause
     */
    public static String getKeyValueClause(final Set<String> keySet, final String separator) {
        return Joiner.on(separator).withKeyValueSeparator("=").join(Maps.asMap(keySet, input -> "?"));
    }
}
