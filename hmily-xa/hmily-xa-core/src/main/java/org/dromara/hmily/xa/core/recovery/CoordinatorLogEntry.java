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

package org.dromara.hmily.xa.core.recovery;

import lombok.Data;
import org.dromara.hmily.repository.spi.entity.HmilyXaRecovery;
import org.dromara.hmily.repository.spi.entity.HmilyXaRecoveryImpl;
import org.dromara.hmily.xa.core.XaState;

import java.time.LocalDateTime;

/**
 * CoordinatorLogEntry .
 *
 * @author sixh chenbin
 */
@Data
public class CoordinatorLogEntry extends RecoveryLog {

    private String globalId;

    private String branchId;

    private Long endBxid;

    private Long endXid;

    private String superCoordinatorId;

    private XaState state;

    private String tmName;

    @Override
    public HmilyXaRecovery getRecovery() {
        HmilyXaRecoveryImpl recovery = new HmilyXaRecoveryImpl();
        recovery.setVersion(1);
        recovery.setIsCoordinator(true);
        recovery.setState(state.getState());
        recovery.setTmUnique(tmName);
        recovery.setSuperId(superCoordinatorId);
        recovery.setUpdateTime(LocalDateTime.now());
        recovery.setEndXid(endXid);
        recovery.setGlobalId(globalId);
        recovery.setBranchId(branchId);
        return recovery;
    }
}
