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

package org.dromara.hmily.repository.spi.entity;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * The type Hmily lock.
 *
 * @author xiaoyu
 */
@Getter
@RequiredArgsConstructor
public class HmilyLock implements Serializable {
    
    private static final long serialVersionUID = -6910542871629586617L;
    
    /**
     * transaction id.
     */
    private final Long transId;
    
    /**
     * participant id.
     */
    private final Long participantId;
    
    /**
     * resource id.
     */
    private final String resourceId;
    
    /**
     * target table name.
     */
    private final String targetTableName;
    
    /**
     * target table pk.
     */
    private final String targetTablePk;
    
    /**
     * Get lock id.
     *
     * @return lock id
     */
    public String getLockId() {
        return Joiner.on(";;").join(resourceId, targetTableName, targetTablePk);
    }
}
