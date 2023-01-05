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
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import org.dromara.hmily.common.utils.DefaultValueUtils;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.context.XaParticipant;
import org.dromara.hmily.core.service.HmilyTransactionHandler;
import org.dromara.hmily.metrics.constant.LabelNames;
import org.dromara.hmily.metrics.reporter.MetricsReporter;
import org.dromara.hmily.xa.core.HmilyXaException;
import org.dromara.hmily.xa.core.HmilyXaResource;
import org.dromara.hmily.xa.core.XaResourcePool;
import org.dromara.hmily.xa.core.XaResourceWrapped;
import org.dromara.hmily.xa.core.XidImpl;
import org.dromara.hmily.xa.rpc.RpcXaProxy;
import org.dromara.hmily.xa.rpc.RpcXaProxy.XaCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * CommitHmilyTransactionHandler .
 * 提交一个事务的相关处理.
 *
 * @author sixh chenbin
 */
public class PrepareHmilyTransactionHandler implements HmilyTransactionHandler {

    private final Logger logger = LoggerFactory.getLogger(PrepareHmilyTransactionHandler.class);

    @Override
    public Object handleTransaction(final ProceedingJoinPoint point, final HmilyTransactionContext hmilyTransactionContext) throws Throwable {
        //完成prepare.
        XaParticipant xaParticipant = hmilyTransactionContext.getXaParticipant();
        String branchId = xaParticipant.getBranchId();
        XidImpl xid = new XidImpl(xaParticipant.getGlobalId(), branchId);
        String globalId = xid.getGlobalId();
        logger.info("Got prepare cmd: {}", xid);
        List<XaResourceWrapped> allResource = XaResourcePool.INST.getAllResource(globalId);
        //如果是远程调用就只能是commit.
        int result;
        try {
            for (final XaResourceWrapped xaResourceWrapped : allResource) {
                int prepare = ((HmilyXaResource) xaResourceWrapped).prepare();
                switch (prepare) {
                    case XAResource.XA_OK:
                        result = RpcXaProxy.YES;
                        break;
                    case XAResource.XA_RDONLY:
                    default:
                        result = RpcXaProxy.NO;
                        break;
                }
                logger.info("Prepare:执行一个事务结果{}:{}", xaResourceWrapped, result);
                if (Objects.equals(result, RpcXaProxy.NO)) {
                    break;
                }
            }
        } catch (Exception ex) {
            logger.info("Prepare:执行一个事务异常", ex);
            throw new HmilyXaException(HmilyXaException.UNKNOWN);
        }
        MetricsReporter.counterIncrement(LabelNames.TRANSACTION_STATUS, new String[]{TransTypeEnum.XA.name(), HmilyRoleEnum.PARTICIPANT.name(), XaCmd.PREPARE.name()});
        Method method = ((MethodSignature) (point.getSignature())).getMethod();
        return DefaultValueUtils.getDefaultValue(method.getReturnType());
    }
}
