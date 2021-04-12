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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TransactionImpl .
 *
 * @author sixh chenbin
 */
public class TransactionImpl implements Transaction {

    private Logger logger = LoggerFactory.getLogger(TransactionImpl.class);

    private XIdImpl xid;

    private SubCoordinator subCoordinator = null;

    private final List<XAResource> enlistResourceList = Collections.synchronizedList(new ArrayList<>());

    private List<XAResource> delistResourceList;

    public TransactionImpl(XIdImpl xid) {
        this.xid = xid;
        buildCoord();
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        if (subCoordinator != null) {
            subCoordinator.commit();
        } else {

        }
    }

    public void doEnList(XAResource xaResource, int flag) throws SystemException, RollbackException {
        //xaResource;
        if (flag == XAResource.TMRESUME) {

        } else if (flag == XAResource.TMJOIN) {
            enlistResource(xaResource);
        }
        delistResourceList = null;
    }

    public void doDeList(int flag) throws SystemException {
        delistResourceList = new ArrayList<>(enlistResourceList);
        for (XAResource resource : delistResourceList) {
            delistResource(resource, flag);
        }
    }

    @Override
    public boolean delistResource(XAResource xaResource, int flag) throws IllegalStateException, SystemException {
        if (!enlistResourceList.contains(xaResource)) {
            return false;
        }
        Xid xid = new XIdImpl();
        try {
            xaResource.end(xid, flag);
        } catch (XAException e) {
        }
        return false;

    }

    @Override
    public boolean enlistResource(XAResource xaResource) throws RollbackException, IllegalStateException, SystemException {
        //is null .
        if (subCoordinator == null) {
            buildCoord();
            if (subCoordinator == null) {
                throw new SystemException("not create subCoordinator");
            }
        }

        XIdImpl resId = this.subCoordinator.nextXid(this.xid);
        HmilyXaResource hmilyXaResource = new HmilyXaResource(resId, xaResource);
        boolean found = subCoordinator.addXaResource(hmilyXaResource);
        int flag = found ? XAResource.TMJOIN : XAResource.TMNOFLAGS;
        try {
            hmilyXaResource.start(flag);
        } catch (XAException e) {
            logger.error("", e);
        }
        return true;
    }

    @Override
    public int getStatus() throws SystemException {
        return 0;
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) throws RollbackException, IllegalStateException, SystemException {

    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {

    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {

    }

    void buildCoord() {
        try {
            subCoordinator = new SubCoordinator(this);

        } catch (Exception ex) {
        }
    }
}
