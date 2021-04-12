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

package org.dromara.hmily.xa.core;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * HmilyXaResource .
 *
 * @author sixh chenbin
 */
public class HmilyXaResource implements XAResource {

    private XAResource xaResource;

    private Xid xid;

    public void setXid(Xid xid) {
        this.xid = xid;
    }

    public Xid getXid() {
        return xid;
    }

    public HmilyXaResource(XAResource xaResource) {
        this.xaResource = xaResource;
    }

    public HmilyXaResource(Xid xid, XAResource xaResource) {
        this.xaResource = xaResource;
        this.xid = xid;
    }

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        this.xaResource.commit(xid, b);
    }

    public void commit(boolean b) throws XAException {
        this.commit(this.xid, b);
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
        this.xaResource.end(xid, i);
    }

    public void end(int i) throws XAException {
        this.end(this.xid, i);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        this.xaResource.forget(xid);
    }

    public void forget() throws XAException {
        this.forget(this.xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return this.xaResource.getTransactionTimeout();
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return this.xaResource.isSameRM(xaResource);
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        return this.xaResource.prepare(xid);
    }

    public int prepare() throws XAException {
        return this.prepare(this.xid);
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        return this.xaResource.recover(i);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        this.xaResource.rollback(xid);
    }

    public void rollback() throws XAException {
        this.rollback(this.xid);
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        return this.xaResource.setTransactionTimeout(i);
    }

    @Override
    public void start(Xid xid, int i) throws XAException {
        this.xaResource.start(xid, i);
    }

    public void start(int i) throws XAException {
        this.start(this.xid, i);
    }
}
