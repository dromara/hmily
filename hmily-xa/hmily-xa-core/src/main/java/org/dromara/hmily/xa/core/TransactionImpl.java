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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TransactionImpl .
 * a thread only .
 *
 * @author sixh chenbin
 */
public class TransactionImpl implements Transaction {

    private final Logger logger = LoggerFactory.getLogger(TransactionImpl.class);

    private final XIdImpl xid;

    private SubCoordinator subCoordinator;

    private final List<XAResource> enlistResourceList = Collections.synchronizedList(new ArrayList<>());

    private List<XAResource> delistResourceList;

    private final TransactionContext context;

    /**
     * Instantiates a new Transaction.
     *
     * @param xId the x id
     */
    public TransactionImpl(final XIdImpl xId) {
        this.xid = xId;
        context = new TransactionContext(null, xId);
        //todo:这里还要设置超时器.
        subCoordinator(true, true);
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        Coordinator coordinator = context.getCoordinator();
        if (coordinator != null) {
            try {
                coordinator.commit();
            } catch (RemoteException e) {
                throw new SystemException(e.getMessage());
            }
        }
        if (subCoordinator != null) {
            subCoordinator.commit();
        }
    }

    /**
     * Do en list.
     *
     * @param xaResource the xa resource
     * @param flag       the flag
     * @throws SystemException   the system exception
     * @throws RollbackException the rollback exception
     */
    public void doEnList(final XAResource xaResource, final int flag) throws SystemException, RollbackException {
        //xaResource;
        if (flag == XAResource.TMJOIN
                || flag == XAResource.TMNOFLAGS) {
            enlistResource(xaResource);
        } else if (flag == XAResource.TMRESUME) {
            //进行事务的恢复.
            if (delistResourceList != null) {
                for (final XAResource resource : delistResourceList) {
                    this.enlistResource(resource);
                }
            }
        }
        delistResourceList = null;

    }

    /**
     * Do de list.
     *
     * @param flag the flag
     * @throws SystemException the system exception
     */
    public void doDeList(final int flag) throws SystemException {
        delistResourceList = new ArrayList<>(enlistResourceList);
        for (XAResource resource : delistResourceList) {
            delistResource(resource, flag);
        }
    }

    @Override
    public boolean delistResource(final XAResource xaResource, final int flag) throws IllegalStateException, SystemException {
        if (!enlistResourceList.contains(xaResource)) {
            return false;
        }
        HmilyXaResource myResoure = (HmilyXaResource) xaResource;
        try {
            //flags - TMSUCCESS、TMFAIL 或 TMSUSPEND 之一。
            myResoure.end(flag);
            enlistResourceList.remove(xaResource);
            return true;
        } catch (XAException e) {
            logger.info("xa resource end ", e);
        }
        return false;
    }

    @Override
    public boolean enlistResource(final XAResource xaResource) throws RollbackException, IllegalStateException, SystemException {
        //is null .
        if (subCoordinator == null) {
            subCoordinator(false, false);
            if (subCoordinator == null) {
                throw new SystemException("not create subCoordinator");
            }
        }
        XIdImpl resId = this.subCoordinator.nextXid(this.xid);
        HmilyXaResource hmilyXaResource = new HmilyXaResource(resId, xaResource);
        boolean found = subCoordinator.addXaResource(hmilyXaResource);
        int flag = found ? XAResource.TMJOIN : XAResource.TMNOFLAGS;
        try {
            if (delistResourceList != null && delistResourceList.contains(hmilyXaResource)) {
                flag = XAResource.TMRESUME;
            }
            // TMNOFLAGS、TMJOIN 或 TMRESUME 之一。
            hmilyXaResource.start(flag);
        } catch (XAException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
        if (!enlistResourceList.contains(hmilyXaResource)) {
            enlistResourceList.add(hmilyXaResource);
        }
        return true;
    }

    @Override
    public int getStatus() throws SystemException {
        if (context.getCoordinator() != null) {
            return context.getCoordinator().getState().getState();
        }
        return subCoordinator.getState().getState();
    }

    @Override
    public void registerSynchronization(final Synchronization synchronization) throws RollbackException, IllegalStateException, SystemException {
        if (synchronization == null) {
            return;
        }
        if (subCoordinator == null) {
            subCoordinator(false, true);
        }
        subCoordinator.addSynchronization(synchronization);
    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {
        Coordinator coordinator = context.getCoordinator();
        if (coordinator != null) {
            coordinator.rollback();
        }
        if (subCoordinator != null) {
            subCoordinator.rollback();
        }
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        Coordinator coordinator = context.getCoordinator();
        if (coordinator != null) {
            coordinator.setRollbackOnly();
        }
        if (subCoordinator == null) {
            subCoordinator(false, false);
        }
        subCoordinator.setRollbackOnly();
    }

    private void subCoordinator(final boolean newCoordinator, final boolean stateActive) {
        try {
            subCoordinator = new SubCoordinator(this);
            if (!stateActive) {
                return;
            }
            Coordinator coordinator = context.getCoordinator();
            if (newCoordinator && coordinator == null) {
                coordinator = new Coordinator(xid);
            }
            context.setCoordinator(coordinator);
            coordinator.addCoordinators(subCoordinator);
        } catch (Exception ex) {
            logger.error("build SubCoordinator error");
        }
    }

    /**
     * Gets xid.
     *
     * @return the xid
     */
    public XIdImpl getXid() {
        return xid;
    }
}
