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

package org.dromara.hmily.repository.spi.entity;

import com.google.common.base.Joiner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The type Hmily lock.
 *
 * @author xiaoyu
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HmilyLock implements Serializable {
    
    private static final long serialVersionUID = -6910542871629586617L;
    
    /**
     * transaction id.
     */
    private Long transId;
    
    /**
     * participant id.
     */
    private Long participantId;
    
    /**
     * resource id.
     */
    private String resourceId;
    
    /**
     * target table name.
     */
    private String targetTableName;
    
    /**
     * target table pk.
     */
    private String targetTablePk;

    /**
     * Get lock id.
     *
     * @return lock id
     */
    public String getLockId() {
        return Joiner.on(";;").join(resourceId, targetTableName, targetTablePk);
    }
}
