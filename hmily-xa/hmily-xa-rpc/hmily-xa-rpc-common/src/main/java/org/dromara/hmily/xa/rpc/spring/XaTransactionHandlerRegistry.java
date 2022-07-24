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

package org.dromara.hmily.xa.rpc.spring;

import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.context.XaParticipant;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.core.service.HmilyTransactionHandlerRegistry;
import org.dromara.hmily.spi.HmilySPI;
import org.dromara.hmily.xa.rpc.RpcXaProxy;

import java.util.EnumMap;
import java.util.Map;

/**
 * XaTransactionHandlerRegistry .
 *
 * @author sixh chenbin
 */
@HmilySPI("xa")
public class XaTransactionHandlerRegistry implements HmilyTransactionHandlerRegistry {

    /**
     * The Enums map.
     */
    private final Map<RpcXaProxy.XaCmd, HmilyTransactionHandler> enumsMap = new EnumMap<>(RpcXaProxy.XaCmd.class);

    /**
     * Instantiates a new Xa transaction handler registry.
     */
    public XaTransactionHandlerRegistry() {
        enumsMap.put(RpcXaProxy.XaCmd.START, new BeginHmilyTransactionHandler());
        enumsMap.put(RpcXaProxy.XaCmd.COMMIT, new CommitHmilyTransactionHandler());
        enumsMap.put(RpcXaProxy.XaCmd.PREPARE, new PrepareHmilyTransactionHandler());
        enumsMap.put(RpcXaProxy.XaCmd.RECOVER, new RecoverHmilyTransactionHandler());
        enumsMap.put(RpcXaProxy.XaCmd.ROLLBACK, new RollbackHmilyTransactionHandler());
    }

    @Override
    public HmilyTransactionHandler select(final HmilyTransactionContext context) {
        HmilyTransactionHandler hmilyTransactionHandler = new DefHmilyTransactionHandler();
        if (context != null && context.getXaParticipant() != null) {
            XaParticipant xaParticipant = context.getXaParticipant();
            String cmd = xaParticipant.getCmd();
            if (!StringUtils.isBlank(cmd)) {
                RpcXaProxy.XaCmd xaCmd = RpcXaProxy.XaCmd.valueOf(cmd);
                if (enumsMap.containsKey(xaCmd)) {
                    hmilyTransactionHandler = enumsMap.get(xaCmd);
                }
            }
        }
        return hmilyTransactionHandler;
    }
}
