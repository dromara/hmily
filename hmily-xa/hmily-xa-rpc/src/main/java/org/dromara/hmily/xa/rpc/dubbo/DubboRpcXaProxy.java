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

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.xa.rpc.RpcXaProxy;
import org.dromara.hmily.xa.rpc.XaParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * DubboRpcXaProxy .
 * 这个用于业务线的提供方,用于tm对事务的调用.
 * 1、包括dubbo服务的注册，dubbo事务的相关处理.
 * 2、当dubbo是一个事务数据源时候，接受相关的指令处理.
 *
 * @author sixh chenbin
 */
public class DubboRpcXaProxy implements RpcXaProxy {

    private final Invoker<?> invoker;

    private final Invocation rpcInvocation;

    private final Logger logger = LoggerFactory.getLogger(DubboRpcXaProxy.class);

    /**
     * 初始化一个调用dubbo的rpc代理.
     *
     * @param invoker       the invoker
     * @param rpcInvocation the rpc invocation
     */
    public DubboRpcXaProxy(final Invoker<?> invoker, final Invocation rpcInvocation) {
        this.invoker = invoker;
        this.rpcInvocation = rpcInvocation;
    }

    @Override
    public int cmd(final Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return NO;
        }
        params.forEach((k, v) -> RpcContext.getContext().setAttachment(k, v.toString()));
        //如果是执行一个cmd的时候.
        Result invoke = this.invoker.invoke(rpcInvocation);
        if (invoke.hasException()) {
            logger.warn("执行一个指令发送了异常，{}:{}", params, invoke.getException().getMessage());
            return EXC;
        } else {
            return YES;
        }
    }

    @Override
    public int getTimeout() {
        return Optional.ofNullable(rpcInvocation.getAttachment("timeOut")).map(Integer::parseInt).orElse(0);
    }


    @Override
    public void init(final XaParticipant participant) {
        RpcMediator.getInstance().transmit(RpcContext.getContext()::setAttachment, participant);
    }
}
