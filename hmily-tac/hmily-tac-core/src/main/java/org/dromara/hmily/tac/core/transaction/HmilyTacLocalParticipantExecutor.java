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

package org.dromara.hmily.tac.core.transaction;

import org.dromara.hmily.core.cache.HmilyParticipantCacheManager;
import org.dromara.hmily.core.hook.UndoHook;
import org.dromara.hmily.core.repository.HmilyRepositoryStorage;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.tac.core.cache.HmilyParticipantUndoCacheManager;
import org.dromara.hmily.tac.core.lock.HmilyLockManager;

import java.util.List;

/**
 * Hmily TAC local participant executor.
 *
 * @author zhaojun
 */
public class HmilyTacLocalParticipantExecutor {
    
    /**
     * Do confirm.
     *
     * @param participant hmily participant
     */
    public static void confirm(final HmilyParticipant participant) {
        List<HmilyParticipantUndo> undoList = HmilyParticipantUndoCacheManager.getInstance().get(participant.getParticipantId());
        for (HmilyParticipantUndo undo : undoList) {
            cleanUndo(undo);
        }
        cleanHmilyParticipant(participant);
    }
    
    /**
     * Do cancel.
     *
     * @param participant hmily participant
     */
    public static void cancel(final HmilyParticipant participant) {
        List<HmilyParticipantUndo> undoList = HmilyParticipantUndoCacheManager.getInstance().get(participant.getParticipantId());
        for (HmilyParticipantUndo undo : undoList) {
            boolean success = UndoHook.INSTANCE.run(undo);
            if (success) {
                cleanUndo(undo);
            }
        }
        cleanHmilyParticipant(participant);
    }
    
    private static void cleanUndo(final HmilyParticipantUndo hmilyParticipantUndo) {
        HmilyRepositoryStorage.removeHmilyParticipantUndo(hmilyParticipantUndo);
        HmilyParticipantUndoCacheManager.getInstance().removeByKey(hmilyParticipantUndo.getParticipantId());
        HmilyLockManager.INSTANCE.releaseLocks(hmilyParticipantUndo.getHmilyLocks());
    }
    
    private static void cleanHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        HmilyParticipantCacheManager.getInstance().removeByKey(hmilyParticipant.getParticipantId());
        HmilyRepositoryStorage.removeHmilyParticipant(hmilyParticipant);
    }
}
