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

package org.dromara.hmily.xa.rpc.dubbo;


import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.dromara.hmily.annotation.HmilyXA;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.xa.core.TransactionImpl;
import org.dromara.hmily.xa.core.TransactionManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * DubboXaFilter .
 *
 * @author sixh chenbin
 */
@Activate(group = {Constants.CONSUMER})
public class DubboXaFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboXaFilter.class);

    @Override
    public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
        LOGGER.debug("create dubbo xa sources");
        Class<?> clazz = invoker.getInterface();
        Class<?>[] args = invocation.getParameterTypes();
        String methodName = invocation.getMethodName();
        try {
            Method method = clazz.getMethod(methodName, args);
            HmilyXA hmily = method.getAnnotation(HmilyXA.class);
            if (Objects.isNull(hmily)) {
                return invoker.invoke(invocation);
            }
        } catch (Exception ex) {
            LogUtil.error(LOGGER, "hmily find method error {} ", ex::getMessage);
            return invoker.invoke(invocation);
        }
        //If it is an xa transaction that can be processed.
        Transaction transaction = TransactionManagerImpl.INST.getTransaction();
        if (transaction instanceof TransactionImpl) {
            XAResource resource = new DubboRpcResource();
            try {
                ((TransactionImpl) transaction).doEnList(resource, XAResource.TMJOIN);
            } catch (SystemException | RollbackException e) {
                LOGGER.error(":", e);
                throw new RuntimeException("dubbo xa resource tm join err", e);
            }
        }
        //todo: 这里要把事务相关信息带过去.
        return invoker.invoke(invocation);
    }
}
