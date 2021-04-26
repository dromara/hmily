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

import org.dromara.hmily.xa.core.timer.HmilyTimer;
import org.dromara.hmily.xa.core.timer.TimerRemovalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * Coordinator .
 *
 * @author sixh chenbin
 */
public class Coordinator implements Resource, Finally, TimerRemovalListener<Resource> {

    private final Logger logger = LoggerFactory.getLogger(Coordinator.class);

    /**
     * all SubCoordinator.
     */
    private final Vector<Resource> coordinators = new Vector<>();

    private final XIdImpl xid;

    private XaState state = XaState.STATUS_ACTIVE;

    /**
     * 事务开始的时间.
     */
    private final LocalDateTime date;

    private final HmilyTimer<Resource> hmilyTimer;
    /**
     * 父协调器.
     */
    private String superCoordinator;

    /**
     * Instantiates a new Coordinator.
     *
     * @param xid the xid
     */
    public Coordinator(final XIdImpl xid) {
        this.xid = xid;
        date = LocalDateTime.now();
        //主事务的问题处理.
        hmilyTimer = new HmilyTimer<>(30000, TimeUnit.SECONDS, xid.getGlobalId());
        hmilyTimer.addRemovalListener(this);
    }

    /**
     * Gets timer.
     *
     * @return the timer
     */
    public HmilyTimer<Resource> getTimer() {
        return hmilyTimer;
    }

    @Override
    public Result prepare() {
        return doPrepare();
    }

    @Override
    public void rollback() {
        switch (state) {
            case STATUS_ACTIVE:
            case STATUS_MARKED_ROLLBACK:
                logger.warn("statue == STATUS_ACTIVE OR STATUS_MARKED_ROLLBACK");
                break;
            case STATUS_ROLLEDBACK:
                logger.warn("statue == STATUS_ROLLED_BACK");
                return;
            default:
                break;
        }
        doRollback();
    }

    @Override
    public void commit() throws RemoteException {
        switch (state) {
            case STATUS_ACTIVE:
                break;
            case STATUS_COMMITTED:
                logger.warn("commit done");
                return;
            case STATUS_ROLLEDBACK:
                logger.warn("commit state == STATUS_ROLLEDBACK");
                //todo:完成.
                throw new TransactionRolledbackException();
            case STATUS_MARKED_ROLLBACK:
                doRollback();
                throw new TransactionRolledbackException();
            default:
                throw new TransactionRequiredException("status error");
        }
        //哪果只有一个数据就表示只是自己本身，@Coordinator.
        if (this.coordinators.size() == 1) {
            state = XaState.STATUS_COMMITTING;
            Resource resource = coordinators.get(0);
            //第一阶段直接提交.
            try {
                resource.onePhaseCommit();
                state = XaState.STATUS_COMMITTED;
            } catch (TransactionRolledbackException rx) {
                state = XaState.STATUS_MARKED_ROLLBACK;
            } catch (Exception ex) {
                logger.error("onPhaseCommit error size to 1", ex);
                state = XaState.STATUS_UNKNOWN;
            }
            return;
        }
        //Start 1 pc.
        Result result = doPrepare();
        if (result == Result.COMMIT) {
            doCommit();
        } else if (result == Result.READONLY) {
            doRollback();
        } else {
            state = XaState.STATUS_COMMITTED;
        }
    }

    @Override
    public void onePhaseCommit() throws RemoteException {
        logger.info("onePhaseCommit{}", this.xid);
        doCommit();
    }

    private Result doPrepare() {
        // 开始 1pc.
        Result rs = Result.READONLY;
        if (coordinators.size() == 0) {
            state = XaState.STATUS_COMMITTED;
            return rs;
        }
        state = XaState.STATUS_PREPARING;
        int errors = 0;
        for (final Resource coordinator : coordinators) {
            /*
             * 开始调用1pc阶段.
             */
            if (errors > 0) {
                break;
            } else {
                try {
                    Result prepare = coordinator.prepare();
                    if (prepare == Result.ROLLBACK) {
                        errors++;
                        rs = Result.ROLLBACK;
                    } else if (prepare == Result.COMMIT) {
                        rs = Result.COMMIT;
                    }
                } catch (Exception ex) {
                    errors++;
                    rs = Result.ROLLBACK;
                }
            }
        }
        if (rs == Result.COMMIT) {
            state = XaState.STATUS_PREPARED;
        } else if (rs == Result.READONLY) {
            state = XaState.STATUS_COMMITTED;
        }
        return rs;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public XaState getState() {
        return state;
    }

    /**
     * Add coordinators boolean.
     *
     * @param resource the remote
     * @return the boolean
     */
    public synchronized boolean addCoordinators(final Resource resource) {
        if (coordinators.contains(resource)) {
            return true;
        }
        boolean add = this.coordinators.add(resource);
        if (add) {
            hmilyTimer.put(resource);
        }
        return add;
    }

    /**
     * Sets rollback only.
     */
    public void setRollbackOnly() {
        if (state == XaState.STATUS_ACTIVE) {
            state = XaState.STATUS_MARKED_ROLLBACK;
        }
    }

    private void doRollback() {
        state = XaState.STATUS_ROLLEDBACK;
        for (Resource resource : this.coordinators) {
            if (resource != null) {
                resource.rollback();
            }
        }
    }

    private void doCommit() throws TransactionRolledbackException {
        state = XaState.STATUS_COMMITTING;
        int commitErrors = 0;
        for (Resource resource : this.coordinators) {
            try {
                resource.commit();
            } catch (RemoteException e) {
                commitErrors++;
            }
        }
        if (commitErrors == 0) {
            state = XaState.STATUS_COMMITTED;
        } else {
            state = XaState.STATUS_ROLLEDBACK;
            throw new TransactionRolledbackException();
        }
    }

    @Override
    public void onRemoval(final Resource value, final Long expire, final Long elapsed) {
        if (value instanceof SubCoordinator) {

        }
    }
}
