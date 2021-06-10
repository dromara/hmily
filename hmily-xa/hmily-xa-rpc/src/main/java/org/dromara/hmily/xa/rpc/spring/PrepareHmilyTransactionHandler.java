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

import org.aspectj.lang.ProceedingJoinPoint;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.context.XaParticipant;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.xa.core.HmliyXaException;
import org.dromara.hmily.xa.core.XaResourcePool;
import org.dromara.hmily.xa.core.XaResourceWrapped;
import org.dromara.hmily.xa.core.XidImpl;
import org.dromara.hmily.xa.rpc.RpcXaProxy;

import javax.transaction.xa.XAException;

/**
 * CommitHmilyTransactionHandler .
 * 提交一个事务的相关处理.
 *
 * @author sixh chenbin
 */
public class PrepareHmilyTransactionHandler implements HmilyTransactionHandler {
    @Override
    public Object handleTransaction(final ProceedingJoinPoint point, final HmilyTransactionContext hmilyTransactionContext) throws Throwable {
        //完成prepare.
        XaParticipant xaParticipant = hmilyTransactionContext.getXaParticipant();
        String branchId = xaParticipant.getBranchId();
        XidImpl xid = new XidImpl(branchId);
        XaResourceWrapped resource = XaResourcePool.INST.getResource(xid);
        //如果是远程调用就只能是commit.
        try {
           return resource.prepare(xid);
        } catch (XAException ex) {
            throw new HmliyXaException(ex.errorCode);
        } catch (Exception ex) {
            throw new HmliyXaException(HmliyXaException.UNKNOWN);
        }
    }
}
