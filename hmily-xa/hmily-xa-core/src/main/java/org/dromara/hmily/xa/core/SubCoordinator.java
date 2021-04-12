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

import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.util.Vector;

/**
 * SubCoordinator .
 *
 * @author sixh chenbin
 */
public class SubCoordinator implements Mock {

    private TransactionImpl transaction;

    private XaState state = XaState.STATUS_ACTIVE;

    private boolean isRoot;
    /**
     * all xaResources.
     */
    private Vector<XAResource> resources = new Vector<>();

    /**
     * Instantiates a new Sub coordinator.
     *
     * @param transaction the transaction
     */
    public SubCoordinator(TransactionImpl transaction) {
        this.transaction = transaction;
    }

    @Override
    public int prepare() {
        return 0;
    }

    @Override
    public void rollback() {
        //处理.
        try {
            this.transaction.doDeList(XAResource.TMSUCCESS);
        } catch (SystemException e) {
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

            }
        }
    }

    @Override
    public void commit() {
    }

    public synchronized XIdImpl nextXid(XIdImpl xId) {
        return xId.newResId(this.resources.size() + 1);
    }

    /**
     * Add a xa Resource.
     *
     * @param xaResource the xa resource
     * @return boolean boolean
     */
    public synchronized boolean addXaResource(XAResource xaResource) {
        switch (state) {
            case STATUS_MARKED_ROLLBACK:
                break;
            case STATUS_ACTIVE:
                break;
            default:
                throw new RuntimeException("status == " + state);
        }
        resources.stream().filter(e -> {
            try {
                return !e.isSameRM(xaResource);
            } catch (XAException xaException) {
                return true;
            }
        }).findFirst().ifPresent(xaResource1 -> resources.add(xaResource));
        return true;
    }
}
