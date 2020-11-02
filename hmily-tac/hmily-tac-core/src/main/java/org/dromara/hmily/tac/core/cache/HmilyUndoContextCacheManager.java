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

package org.dromara.hmily.tac.core.cache;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.repository.spi.entity.HmilyDataSnapshot;
import org.dromara.hmily.tac.core.context.HmilyUndoContext;

/**
 * The enum Hmily undo context cache manager.
 *
 * @author xiaoyu
 */
public enum HmilyUndoContextCacheManager {
    
    /**
     * Instance hmily undo context cache manager.
     */
    INSTANCE;
    
    private static final ThreadLocal<List<HmilyUndoContext>> CURRENT_LOCAL = ThreadLocal.withInitial(CopyOnWriteArrayList::new);
    
    /**
     * Set undo context.
     *
     * @param transactionContext transaction context
     * @param dataSnapshot data snapshot
     * @param resourceId resource id
     */
    public void set(final HmilyTransactionContext transactionContext, final HmilyDataSnapshot dataSnapshot, final String resourceId) {
        HmilyUndoContext undoContext = new HmilyUndoContext();
        undoContext.setDataSnapshot(dataSnapshot);
        undoContext.setResourceId(resourceId);
        undoContext.setTransId(transactionContext.getTransId());
        undoContext.setParticipantId(transactionContext.getParticipantId());
        CURRENT_LOCAL.get().add(undoContext);
    }
    
    
    /**
     * Get hmily undo context.
     *
     * @return the hmily undo context list
     */
    public List<HmilyUndoContext> get() {
        return CURRENT_LOCAL.get();
    }
    
    /**
     * clean threadLocal for gc.
     */
    public void remove() {
        CURRENT_LOCAL.remove();
    }
}
