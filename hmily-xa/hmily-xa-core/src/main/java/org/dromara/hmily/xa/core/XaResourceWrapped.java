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
 * XaResourceExtends .
 * 扩展 XaResources扩展.
 *
 * @author sixh chenbin
 */
public abstract class XaResourceWrapped implements XAResource {

    /**
     * 获取一个事务类型的名称..
     *
     * @return the name
     */
    public abstract String getName();

    @Override
    public void start(final Xid xid, final int flag) throws XAException {
        XaResourcePool.INST.addResource(xid, this);
        start0(xid, flag);
    }

    @Override
    public void commit(final Xid xid, final boolean onePhase) throws XAException {
        commit0(xid, onePhase);
        XaResourcePool.INST.removeResource(xid);
    }

    @Override
    public void rollback(final Xid xid) throws XAException {
        rollback0(xid);
        XaResourcePool.INST.removeResource(xid);
    }

    /**
     * 子类实现. Start 0.
     *
     * @param xid  the xid
     * @param flag the flag
     * @throws XAException the xa exception
     */
    void start0(final Xid xid, final int flag) throws XAException {

    }

    /**
     * 子类实现. Commit 0.
     *
     * @param xid      the xid
     * @param onePhase the one phase
     * @throws XAException the xa exception
     */
    void commit0(final Xid xid, final boolean onePhase) throws XAException {

    }

    /**
     * 子类实现. Rollback 0.
     *
     * @param xid the xid
     * @throws XAException the xa exception
     */
    void rollback0(final Xid xid) throws XAException {

    }
}
