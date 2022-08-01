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

import javax.sql.XAConnection;
import javax.transaction.*;
import java.util.HashSet;
import java.util.Set;

/**
 * TransactionManagerImpl .
 *
 * @author sixh chenbin
 */
public enum TransactionManagerImpl implements TransactionManager {

    /**
     * Singleton.
     */
    INST;

    private final ThreadLocal<Set<XAConnection>> enlisted = ThreadLocal.withInitial(HashSet::new);

    private HmilyXaTransactionManager hmilyXaTransactionManager;

    TransactionManagerImpl() {
        this.initialized();
    }

    /**
     * Initialized.
     */
    public void initialized() {
        //initialize_it @see HmilyXaTransactionManager;
        hmilyXaTransactionManager = HmilyXaTransactionManager.initialized();
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        hmilyXaTransactionManager.begin();
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        hmilyXaTransactionManager.commit();
    }

    @Override
    public int getStatus() throws SystemException {
        return hmilyXaTransactionManager.getState();
    }

    @Override
    public Transaction getTransaction() {
        return hmilyXaTransactionManager.getTransaction();
    }

    @Override
    public void resume(final Transaction transaction) throws InvalidTransactionException, IllegalStateException, SystemException {
        hmilyXaTransactionManager.resume(transaction);
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        hmilyXaTransactionManager.rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        hmilyXaTransactionManager.setRollbackOnly();
    }

    @Override
    public void setTransactionTimeout(final int i) throws SystemException {
        hmilyXaTransactionManager.setTransactionTimeout(i);
    }

    @Override
    public Transaction suspend() throws SystemException {
        return hmilyXaTransactionManager.suspend();
    }

    /**
     * Is exist data sources boolean.
     *
     * @param connection the connection
     * @return the boolean
     */
    public boolean isExistDataSources(final XAConnection connection) {
        boolean contains = enlisted.get().contains(connection);
        Transaction transaction = getTransaction();
        if (!contains) {
            try {
                transaction.registerSynchronization(new Synchronization() {
                    @Override
                    public void beforeCompletion() {
                        enlisted.get().remove(connection);
                    }

                    @Override
                    public void afterCompletion(final int status) {
                        enlisted.get().clear();
                        enlisted.remove();
                    }
                });
            } catch (RollbackException | SystemException e) {
                return false;
            }
            enlisted.get().add(connection);
        }
        return contains;
    }
}
