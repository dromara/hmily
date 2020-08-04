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

package org.dromara.hmily.tac.datasource.executor;

import org.dromara.hmily.core.hook.UndoHook;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.tac.datasource.HmilyDatasource;
import org.dromara.hmily.tac.datasource.manager.HmilyDatasourceManager;

public class HmilyTacRollbackExecutor {
    
    private static volatile HmilyTacRollbackExecutor instance = null;
    
    private HmilyTacRollbackExecutor(){
        registerRollback();
    }
    
    public static HmilyTacRollbackExecutor getInstance() {
        if (instance == null) {
            synchronized (HmilyTacRollbackExecutor.class) {
                if (instance == null) {
                    instance = new HmilyTacRollbackExecutor();
                }
            }
        }
        return instance;
    }
    
    private void registerRollback() {
        UndoHook.INSTANCE.register(undo -> {
            HmilyDatasource hmilyDatasource = HmilyDatasourceManager.get(undo.getResourceId());
            doRollback(hmilyDatasource, undo);
            return true;
        });
    }
    
    private void doRollback(final HmilyDatasource hmilyDatasource , final HmilyParticipantUndo undo) {
    
    }
    
}
