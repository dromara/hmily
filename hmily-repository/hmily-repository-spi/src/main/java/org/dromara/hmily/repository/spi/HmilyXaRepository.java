/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dromara.hmily.repository.spi;

import org.dromara.hmily.repository.spi.entity.HmilyXaRecovery;

import java.util.List;

/**
 * HmilyXaRepository .
 * xa相关的查询.
 *
 * @author sixh chenbin
 */
public interface HmilyXaRepository extends HmilyRepository {

    /**
     * Query by tm unique list.
     *
     * @param tmUnique the tm unique
     * @param state    the state
     * @return the list
     */
    List<HmilyXaRecovery> queryByTmUnique(String tmUnique, Integer state);

    /**
     * Add log.
     *
     * @param log the log
     */
    void addLog(HmilyXaRecovery log);
}
