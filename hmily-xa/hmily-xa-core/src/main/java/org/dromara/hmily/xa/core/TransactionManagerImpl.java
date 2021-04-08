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

import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

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

    HmilyXaTransactionManager hmilyXaTransactionManager = null;

    TransactionManagerImpl() {
    }

    public void initialized(DataSource dataSource) {
        //初始化一下 @see HmilyXaTransactionManager;
        hmilyXaTransactionManager = HmilyXaTransactionManager.initialized(dataSource);
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        //开始一个事务.
        Transaction transaction = hmilyXaTransactionManager.getTransaction();
        if (transaction == null) {
            transaction = hmilyXaTransactionManager.createTransaction();
        }
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        Transaction transaction = hmilyXaTransactionManager
                .getTransaction();
        if (transaction != null) {
            transaction.commit();
        }
    }

    @Override
    public int getStatus() throws SystemException {
        return 0;
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        return null;
    }

    @Override
    public void resume(Transaction transaction) throws InvalidTransactionException, IllegalStateException, SystemException {

    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        Transaction transaction = hmilyXaTransactionManager
                .getTransaction();
        if (transaction != null) {
            transaction.rollback();
        }
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {

    }

    @Override
    public void setTransactionTimeout(int i) throws SystemException {

    }

    @Override
    public Transaction suspend() throws SystemException {
        return null;
    }
}
