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

import org.dromara.hmily.xa.core.timer.TimerRemovalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionRolledbackException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TransactionImpl .
 * a thread only .
 *
 * @author sixh chenbin
 */
public class TransactionImpl implements Transaction, TimerRemovalListener<Resource> {

    private final Logger logger = LoggerFactory.getLogger(TransactionImpl.class);

    private final XidImpl xid;

    private SubCoordinator subCoordinator;

    private final List<XAResource> enlistResourceList = Collections.synchronizedList(new ArrayList<>());

    private List<XAResource> delistResourceList;

    private final TransactionContext context;

    private final boolean hasSuper;

    /**
     * Instantiates a new Transaction.
     *
     * @param xId the x id
     */
    TransactionImpl(final XidImpl xId, final boolean hasSuper) {
        this.xid = xId;
        this.hasSuper = hasSuper;
        context = new TransactionContext(null, xId);
        //todo:这里还要设置超时器.
        subCoordinator(true, true);
    }

    /**
     * Coordinator
     * Instantiates a new Transaction.
     *
     * @param impl the
     */
    private TransactionImpl(final TransactionImpl impl) {
        this.xid = impl.getXid().newBranchId();
        context = impl.getContext();
        this.delistResourceList = null;
        hasSuper = impl.hasSuper;
        subCoordinator(true, true);
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        Finally oneFinally = context.getOneFinally();
        if (oneFinally != null) {
            try {
                if (hasSuper) {
                    doDeList(XAResource.TMSUCCESS);
                } else {
                    oneFinally.commit();
                }
            } catch (TransactionRolledbackException e) {
                logger.error("error rollback", e);
                throw new RollbackException();
            } catch (RemoteException e) {
                logger.error("error", e);
                throw new SystemException(e.getMessage());
            }
            return;
        }
        if (subCoordinator != null) {
            try {
                //第1阶段提交.
                subCoordinator.onePhaseCommit();
            } catch (TransactionRolledbackException e) {
                logger.error("onePhaseCommit()");
                throw new RollbackException();
            }
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
            //这里需要处理不同的xa事务数据.
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
    public boolean delistResource(final XAResource xaResource, final int flag) throws
            IllegalStateException, SystemException {
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
            logger.info("xa resource end,{}", HmilyXaException.getMessage(e), e);
        }
        return false;
    }

    @Override
    public boolean enlistResource(final XAResource xaResource) throws
            RollbackException, IllegalStateException, SystemException {
        //is null .
        if (subCoordinator == null) {
            subCoordinator(false, false);
            if (subCoordinator == null) {
                throw new SystemException("not create subCoordinator");
            }
        }
        XidImpl resId = this.subCoordinator.nextXid(this.xid);
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
            logger.error("{}", HmilyXaException.getMessage(e), e);
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
    public void registerSynchronization(final Synchronization synchronization) throws
            RollbackException, IllegalStateException, SystemException {
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
        Finally oneFinally = context.getOneFinally();
        if (oneFinally != null) {
            if (hasSuper) {
                doDeList(XAResource.TMSUCCESS);
            } else {
                oneFinally.rollback();
            }
            return;
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
            subCoordinator = new SubCoordinator(this, this.hasSuper);
            if (!stateActive) {
                return;
            }
            Coordinator coordinator = context.getCoordinator();
            if (newCoordinator && coordinator == null) {
                coordinator = new Coordinator(xid, this.hasSuper);
                context.setCoordinator(coordinator);
                if (context.getOneFinally() == null) {
                    context.setOneFinally(coordinator);
                }
                coordinator.getTimer().put(coordinator, 30000, TimeUnit.SECONDS);
                coordinator.getTimer().addRemovalListener(this);
            }
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
    public XidImpl getXid() {
        return xid;
    }

    /**
     * Gets context.
     *
     * @return the context
     */
    public TransactionContext getContext() {
        return context;
    }

    /**
     * 创建一个子任务.
     *
     * @return the transaction
     */
    public TransactionImpl createSubTransaction() {
        return new TransactionImpl(this);
    }

    @Override
    public void onRemoval(final Resource value, final Long expire, final Long elapsed) {
    }
}
