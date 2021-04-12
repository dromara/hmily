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

import java.util.Vector;

/**
 * Coordinator .
 *
 * @author sixh chenbin
 */
public class Coordinator implements Mock {

    /**
     * all SubCoordinator.
     */
    private Vector<Mock> coordinators = new Vector<>();

    private XIdImpl xid;

    private XaState state = XaState.STATUS_ACTIVE;

    private Coordinator superCoord;

    public Coordinator(XIdImpl xid, Coordinator superCoord) {
        this.xid = xid;
        this.superCoord = superCoord;
        coordinators.add(this);
    }

    @Override
    public int prepare() {
        return 0;
    }

    @Override
    public void rollback() {
        switch (state) {
            case STATUS_ACTIVE:
            case STATUS_MARKED_ROLLBACK:
                break;
            case STATUS_ROLLEDBACK:
                return;
        }
        doRollback();
    }

    @Override
    public void commit() {

    }

    public XaState getState() {
        return state;
    }

    public synchronized boolean addCoordinators(Mock mock) {
        if (coordinators.contains(mock)) {
            return true;
        }
        return this.coordinators.add(mock);
    }

    public XIdImpl getSubXid() {
        return this.xid.newBranchId();
    }

    private void doRollback() {
        state = XaState.STATUS_ROLLEDBACK;
        for (int i = 0; i < this.coordinators.size(); i++) {
            Mock mock = this.coordinators.get(i);
            if (mock != null) {
                mock.rollback();
            }
        }
    }

    private void doCommit() {
        state = XaState.STATUS_COMMITTED;
    }
}
