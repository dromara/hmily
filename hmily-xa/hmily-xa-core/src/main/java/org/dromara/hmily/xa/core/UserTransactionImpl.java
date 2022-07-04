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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * UserTransactionImpl .
 * 用于配置事务管理器
 * UserTransaction是支持用户直接通过JTA编写分布式事务的api
 *
 * @author sixh chenbin
 */
public class UserTransactionImpl implements UserTransaction, TransactionManager {

    private TransactionManager tm;

    private TransactionManager getTm() {
        if (tm == null) {
            tm = TransactionManagerImpl.INST;
        }
        return tm;
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        getTm().begin();
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        getTm().commit();
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        getTm().rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        getTm().setRollbackOnly();
    }

    @Override
    public int getStatus() throws SystemException {
        return getTm().getStatus();
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        return getTm().getTransaction();
    }

    @Override
    public void resume(final Transaction transaction) throws InvalidTransactionException, IllegalStateException, SystemException {
        getTm().resume(transaction);
    }

    @Override
    public void setTransactionTimeout(final int i) throws SystemException {
        getTm().setTransactionTimeout(i);
    }

    @Override
    public Transaction suspend() throws SystemException {
        return getTm().suspend();
    }
}
