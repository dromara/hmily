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
public class HmilyXaResource implements XaResourceWrapped {

    private final XAResource xaResource;

    private final Xid xid;

    /**
     * Instantiates a new Hmily xa resource.
     *
     * @param xid        the xid
     * @param xaResource the xa resource
     */
    public HmilyXaResource(final Xid xid,
                           final XAResource xaResource) {
        this.xaResource = xaResource;
        this.xid = xid;
    }

    /**
     * Gets xid.
     *
     * @return the xid
     */
    public Xid getXid() {
        return xid;
    }

    @Override
    public void commit(final Xid xid, final boolean b) throws XAException {
        this.xaResource.commit(xid, b);
    }

    /**
     * Commit.
     *
     * @param b the b
     * @throws XAException the xa exception
     */
    public void commit(final boolean b) throws XAException {
        this.commit(this.xid, b);
    }

    @Override
    public void end(final Xid xid, final int i) throws XAException {
        this.xaResource.end(xid, i);
    }

    /**
     * End.
     *
     * @param i the
     * @throws XAException the xa exception
     */
    public void end(final int i) throws XAException {
        this.end(this.xid, i);
    }

    @Override
    public void forget(final Xid xid) throws XAException {
        this.xaResource.forget(xid);
    }

    /**
     * Forget.
     *
     * @throws XAException the xa exception
     */
    public void forget() throws XAException {
        this.forget(this.xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return this.xaResource.getTransactionTimeout();
    }

    @Override
    public boolean isSameRM(final XAResource xaResource) throws XAException {
        return this.xaResource.isSameRM(xaResource);
    }

    @Override
    public int prepare(final Xid xid) throws XAException {
        return this.xaResource.prepare(xid);
    }

    /**
     * Prepare int.
     *
     * @return the int
     * @throws XAException the xa exception
     */
    public int prepare() throws XAException {
        return this.prepare(this.xid);
    }

    @Override
    public Xid[] recover(final int i) throws XAException {
        return this.xaResource.recover(i);
    }

    @Override
    public void rollback(final Xid xid) throws XAException {
        this.xaResource.rollback(xid);
    }

    /**
     * Rollback.
     *
     * @throws XAException the xa exception
     */
    public void rollback() throws XAException {
        this.rollback(this.xid);
    }

    @Override
    public boolean setTransactionTimeout(final int i) throws XAException {
        return this.xaResource.setTransactionTimeout(i);
    }

    @Override
    public void start(final Xid xid, final int i) throws XAException {
        this.xaResource.start(xid, i);
    }

    /**
     * Start.
     *
     * @param i the
     * @throws XAException the xa exception
     */
    public void start(final int i) throws XAException {
        this.start(this.xid, i);
    }

    @Override
    public String getName() {
        //如果是自己定义的就获取他的名称.
        if (xaResource instanceof XaResourceWrapped) {
            return ((XaResourceWrapped) xaResource).getName();
        }
        return "local";
    }
}
