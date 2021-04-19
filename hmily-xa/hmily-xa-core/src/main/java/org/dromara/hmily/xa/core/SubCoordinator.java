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

import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionRolledbackException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;

import static org.dromara.hmily.xa.core.XaState.STATUS_PREPARED;
import static org.dromara.hmily.xa.core.XaState.STATUS_ROLLING_BACK;

/**
 * SubCoordinator .
 *
 * @author sixh chenbin
 */
public class SubCoordinator implements Remote {

    private final Logger logger = LoggerFactory.getLogger(SubCoordinator.class);

    private final TransactionImpl transaction;

    private XaState state = XaState.STATUS_ACTIVE;

    /**
     * all xaResources.
     */
    private final Vector<XAResource> resources = new Vector<>();

    /**
     * all Synchronization.
     *
     * @see SynchronizationImpl
     */
    private final Vector<Synchronization> synchronizations = new Vector<>();

    private final XIdImpl xId;

    /**
     * Instantiates a new Sub coordinator.
     *
     * @param transaction the transaction
     */
    public SubCoordinator(final TransactionImpl transaction) {
        this.transaction = transaction;
        xId = transaction.getXid().newBranchId();
    }

    @Override
    public Result prepare() {
        try {
            // xa end.
            transaction.doDeList(XAResource.TMSUCCESS);
        } catch (Exception exception) {
            logger.error("do delist error", exception);
        }
        switch (state) {
            case STATUS_MARKED_ROLLBACK:
                state = STATUS_ROLLING_BACK;
                return Result.ROLLBACK;
            case STATUS_COMMITTED:
                return Result.COMMIT;
            default:
                break;
        }
        return doPrepare();
    }

    private synchronized Result doPrepare() {
        Result result = Result.READONLY;
        if (resources.size() == 0) {
            state = XaState.STATUS_COMMITTED;
            return result;
        }
        state = XaState.STATUS_PREPARING;
        boolean isError = false;
        for (int i = 0; i < resources.size(); i++) {
            HmilyXaResource xaResource = (HmilyXaResource) resources.elementAt(i);
            //If there is an error.
            if (isError) {
                try {
                    xaResource.rollback();
                } catch (XAException e) {
                    logger.error("call xa.rollback error ", e);
                }
            } else {
                try {
                    int prepare = xaResource.prepare();
                    switch (prepare) {
                        case XAResource.XA_OK:
                            result = Result.COMMIT;
                            break;
                        case XAResource.XA_RDONLY:
                            result = Result.ROLLBACK;
                            break;
                        default:
                            break;
                    }
                } catch (XAException e) {
                    result = Result.ROLLBACK;
                    isError = true;
                }
            }
        }
        switch (result) {
            case ROLLBACK:
                state = XaState.STATUS_ROLLING_BACK;
                break;
            case COMMIT:
                state = STATUS_PREPARED;
                break;
            case READONLY:
                state = XaState.STATUS_COMMITTED;
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public void rollback() {
        //处理.
        try {
            this.transaction.doDeList(XAResource.TMSUCCESS);
        } catch (SystemException e) {
            logger.error("rollback error", e);
            return;
        }
        switch (state) {
            case STATUS_ACTIVE:
            case STATUS_MARKED_ROLLBACK:
            case STATUS_ROLLING_BACK:
            case STATUS_PREPARED:
                break;
            case STATUS_ROLLEDBACK:
                return;
            default:
                return;

        }
        this.doRollback();
    }

    private synchronized void doRollback() {
        state = XaState.STATUS_ROLLEDBACK;
        for (int i = 0; i < resources.size(); i++) {
            HmilyXaResource xaResource = (HmilyXaResource) resources.elementAt(i);
            try {
                xaResource.rollback();
            } catch (XAException e) {
                logger.error("rollback  error");
            }
        }
    }

    /**
     * 2pc.
     */
    private synchronized void doCommit() {
        state = XaState.STATUS_COMMITTING;
        for (int i = 0; i < resources.size(); i++) {
            HmilyXaResource xaResource = (HmilyXaResource) resources.elementAt(i);
            try {
                // false is 2pc.
                xaResource.commit(false);
            } catch (XAException e) {
                logger.error("rollback  error");
            }
        }
        state = XaState.STATUS_COMMITTED;
    }

    @Override
    public void commit() {
        switch (state) {
            case STATUS_PREPARED:
                break;
            default:
                return;
        }
        doCommit();
    }

    @Override
    public void onePhaseCommit() throws TransactionRolledbackException {
        if (state == XaState.STATUS_ROLLEDBACK) {
            try {
                transaction.doDeList(XAResource.TMSUCCESS);
            } catch (SystemException e) {
                logger.error("error doDeList error", e);
                throw new TransactionRolledbackException(e.getMessage());
            }
        }
        if (state == XaState.STATUS_MARKED_ROLLBACK) {
            try {
                //todo 完成事务前的处理.
                transaction.doDeList(XAResource.TMSUCCESS);
            } catch (SystemException e) {
                logger.error("error doDeList error", e);
            }
            doRollback();
            throw new TransactionRolledbackException();
        }
        if (state == XaState.STATUS_COMMITTED) {
            try {
                transaction.doDeList(XAResource.TMSUCCESS);
            } catch (SystemException e) {
                logger.error("error doDeList error", e);
            }
            return;
        }
        try {
            transaction.doDeList(XAResource.TMSUCCESS);
        } catch (SystemException e) {
            logger.error("deList xaResource:{}", e);
        }
        if (resources.size() == 1) {
            doOnePhaseCommit();
            return;
        }
        Result result = doPrepare();
        if (result == Result.COMMIT) {
            doCommit();
        } else if (result == Result.READONLY) {
            //这里需要处理一下.
        } else if (result == Result.ROLLBACK) {
            this.doRollback();
            throw new TransactionRolledbackException();
        }
    }

    /**
     * 表示为第一阶断的直接提交.
     */
    private void doOnePhaseCommit() throws TransactionRolledbackException {
        state = XaState.STATUS_COMMITTING;
        HmilyXaResource xaResource = (HmilyXaResource) resources.get(0);
        try {
            xaResource.commit(true);
            state = XaState.STATUS_COMMITTED;
        } catch (XAException ex) {
            state = XaState.STATUS_UNKNOWN;
            logger.error("xa commit error{}", xaResource);
            if (Objects.equals(ex.errorCode, XAException.XA_RBROLLBACK)) {
                throw new TransactionRolledbackException("XAException:" + ex.getMessage());
            }
            throw new RuntimeException("XAException" + ex.getMessage());
        }
    }

    /**
     * Next xid x id.
     *
     * @param xId the x id
     * @return the x id
     */
    public synchronized XIdImpl nextXid(final XIdImpl xId) {
        return xId.newResId(this.resources.size() + 1);
    }

    /**
     * Add a xa Resource.
     *
     * @param xaResource the xa resource
     * @return boolean boolean
     */
    public synchronized boolean addXaResource(final XAResource xaResource) {
        switch (state) {
            case STATUS_MARKED_ROLLBACK:
                break;
            case STATUS_ACTIVE:
                break;
            default:
                throw new RuntimeException("status == " + state);
        }
        Optional<XAResource> isSame = resources.stream().filter(e -> {
            try {
                return e.isSameRM(xaResource);
            } catch (XAException xaException) {
                logger.error("xa isSameRM", xaException);
                return false;
            }
        }).findFirst();
        if (!isSame.isPresent()) {
            this.resources.add(xaResource);
            return false;
        }
        return true;
    }

    /**
     * Add synchronization boolean.
     *
     * @param synchronization the synchronization
     * @throws RollbackException the rollback exception
     */
    public synchronized void addSynchronization(final Synchronization synchronization) throws RollbackException {
        if (state == XaState.STATUS_ACTIVE) {
            synchronizations.add(synchronization);
            return;
        }
        if (state == XaState.STATUS_MARKED_ROLLBACK || state == XaState.STATUS_ROLLEDBACK) {
            synchronizations.add(synchronization);
            throw new RollbackException();
        }
    }

    /**
     * Sets rollback only.
     */
    public void setRollbackOnly() {
        if (state == XaState.STATUS_PREPARING) {
            state = XaState.STATUS_MARKED_ROLLBACK;
        }
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public XaState getState() {
        return state;
    }
}
