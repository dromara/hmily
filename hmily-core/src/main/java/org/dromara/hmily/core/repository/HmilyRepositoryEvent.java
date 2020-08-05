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

package org.dromara.hmily.core.repository;

import java.io.Serializable;
import lombok.Data;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;

/**
 * The HmilyTransactionEvent.
 *
 * @author xiaoyu(Myth)
 */
@Data
public class HmilyRepositoryEvent implements Serializable {
    
    private HmilyTransaction hmilyTransaction;
    
    private HmilyParticipant hmilyParticipant;
    
    private HmilyParticipantUndo hmilyParticipantUndo;
    
    private Long transId;
    
    private int type;
    
    /**
     * help gc.
     */
    public void clear() {
        hmilyTransaction = null;
        hmilyParticipant = null;
        hmilyParticipantUndo = null;
        transId = null;
    }
}
