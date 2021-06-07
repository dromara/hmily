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

package org.dromara.hmily.tac.core.context;

import com.google.common.base.Joiner;
import lombok.Data;
import org.dromara.hmily.repository.spi.entity.HmilyDataSnapshot;
import org.dromara.hmily.repository.spi.entity.HmilyLock;

import java.util.Collection;
import java.util.stream.Collectors;

@Data
public class HmilyUndoContext {
    
    /**
     * participant id.
     */
    private Long participantId;
    
    /**
     * transaction id.
     */
    private Long transId;
    
    /**
     * resource id.
     */
    private String resourceId;
    
    /**
     * data snapshot.
     */
    private HmilyDataSnapshot dataSnapshot;
    
    
    /**
     * Get hmily locks.
     *
     * @return hmily locks
     */
    public Collection<HmilyLock> getHmilyLocks() {
        return dataSnapshot.getTuples().stream()
            .map(tuple -> new HmilyLock(transId, participantId, resourceId, tuple.getTableName(), Joiner.on("_").join(tuple.getPrimaryKeyValues()))).collect(Collectors.toList());
    }
}
