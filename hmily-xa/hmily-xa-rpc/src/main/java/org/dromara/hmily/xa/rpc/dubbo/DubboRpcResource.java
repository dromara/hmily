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

import org.dromara.hmily.xa.rpc.RpcResource;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * DubboRpcResource .
 *
 * @author sixh chenbin
 */
public class DubboRpcResource extends RpcResource {

    @Override
    public void commit(final Xid xid, final boolean b) throws XAException {

    }

    @Override
    public void end(final Xid xid, final int i) throws XAException {

    }

    @Override
    public void forget(final Xid xid) throws XAException {

    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return 0;
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
    public void rollback(final Xid xid) throws XAException {

    }

    @Override
    public boolean setTransactionTimeout(final int i) throws XAException {
        return false;
    }

    @Override
    public void start(final Xid xid, final int i) throws XAException {

    }
}
