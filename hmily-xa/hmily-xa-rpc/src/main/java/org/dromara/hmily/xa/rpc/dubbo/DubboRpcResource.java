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
import org.dromara.hmily.xa.rpc.RpcResource;
import org.dromara.hmily.xa.rpc.RpcXaProxy;
import org.dromara.hmily.xa.rpc.XaParticipant;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * DubboRpcResource .
 *
 * @author sixh chenbin
 */
public class DubboRpcResource extends RpcResource {

    /**
     * 用于rpc真实调用的处理.
     */
    private final RpcXaProxy xaProxy;

    /**
     * Instantiates a new Dubbo rpc resource.
     */
    public DubboRpcResource(Invoker<?> invoker, Invocation invocation) {
        this.xaProxy = new DubboRpcXaProxy(invoker, invocation);
    }

    @Override
    public void end(final Xid xid, final int i) throws XAException {

    }

    @Override
    public void forget(final Xid xid) throws XAException {

    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return xaProxy.getTimeout();
    }

    @Override
    public boolean isSameRM(final XAResource xaResource) throws XAException {
        return false;
    }

    @Override
    public int prepare(final Xid xid) throws XAException {
        return 0;
    }

    @Override
    public Xid[] recover(final int i) throws XAException {
        return new Xid[0];
    }

    @Override
    public boolean setTransactionTimeout(final int i) throws XAException {
        //不需要实现
        return true;
    }

    @Override
    public void start(final Xid xid, final int i) throws XAException {
        //需要初始化一下错误调用的相关数据.
        XaParticipant xaParticipant = new XaParticipant();
        xaParticipant.setFlag(i);
        xaParticipant.setBranchId(new String(xid.getBranchQualifier()));
        xaParticipant.setGlobalId(new String(xid.getGlobalTransactionId()));
    }



    @Override
    public String getName() {
        return "dubbo";
    }
}
