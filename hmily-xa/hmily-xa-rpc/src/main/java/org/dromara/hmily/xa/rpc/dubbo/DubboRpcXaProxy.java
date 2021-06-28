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
import org.dromara.hmily.common.constant.CommonConstant;
import org.dromara.hmily.common.utils.GsonUtils;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.context.XaParticipant;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.xa.rpc.RpcXaProxy;
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
    private final Logger logger = LoggerFactory.getLogger(DubboRpcXaProxy.class);

    private final Invoker<?> invoker;

    private final Invocation rpcInvocation;

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

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return invoker.getUrl().toString();
    }

    @Override
    public Integer cmd(final XaCmd cmd, final Map<String, Object> params) {
        if (cmd == null) {
            logger.warn("cmd is null");
            return NO;
        }
        //因为@see HmilyTransactionContext#xaParticipant#cmd字段.
        //CommonConstant.HMILY_TRANSACTION_CONTEXT
        //如果是执行一个cmd的时候.
        String attachment = rpcInvocation.getAttachment(CommonConstant.HMILY_TRANSACTION_CONTEXT);
        //重新设置值.
        if (StringUtils.isNoneBlank(attachment)) {
            HmilyTransactionContext context = GsonUtils.getInstance().fromJson(attachment, HmilyTransactionContext.class);
            context.getXaParticipant().setCmd(cmd.name());
            RpcMediator.getInstance().transmit(RpcContext.getContext()::setAttachment, context);
        }
        Result result = this.invoker.invoke(rpcInvocation);
        if (result.hasException()) {
            logger.warn("执行一个指令发送了异常，{}:{}", params, result.getException().getMessage());
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
        HmilyTransactionContext context = new HmilyTransactionContext();
        context.setXaParticipant(participant);
        RpcMediator.getInstance().transmit(RpcContext.getContext()::setAttachment, context);
    }

    @Override
    public boolean equals(final RpcXaProxy xaProxy) {
        if (xaProxy instanceof DubboRpcXaProxy) {
            return ((DubboRpcXaProxy) xaProxy).getUrl().equals(this.getUrl());
        }
        return false;
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }
}
