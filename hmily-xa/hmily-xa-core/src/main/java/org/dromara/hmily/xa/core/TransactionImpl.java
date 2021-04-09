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

import lombok.SneakyThrows;

import javax.sql.XAConnection;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TransactionImpl .
 *
 * @author sixh chenbin
 */
public class TransactionImpl implements Transaction {

    private XIdImpl xid;

    private XAResource xaResource;
    /**
     * connection;
     */
    private XAConnection connection;

    private SubCoordinator subCoordinator = null;

    private List<XAResource> enlistResource = Collections.synchronizedList(new ArrayList<>());

    private List<XAResource> delistResource = Collections.synchronizedList(new ArrayList<>());

    public TransactionImpl(XIdImpl xid) {
        this.xid = xid;
    }

    @SneakyThrows
    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
    }

    @Override
    public boolean delistResource(XAResource xaResource, int i) throws IllegalStateException, SystemException {
        return false;
    }

    @Override
    public boolean enlistResource(XAResource xaResource) throws RollbackException, IllegalStateException, SystemException {
        return false;
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
}
