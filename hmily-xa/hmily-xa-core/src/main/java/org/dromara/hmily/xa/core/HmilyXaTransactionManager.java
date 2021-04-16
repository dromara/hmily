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

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HmilyXaTransactionManager .
 *
 * @author sixh chenbin
 */
public class HmilyXaTransactionManager {

    private final Logger logger = LoggerFactory.getLogger(HmilyXaTransactionManager.class);

    private final ThreadLocal<Transaction> tms = new ThreadLocal<>();

    private final Map<XIdImpl, Transaction> xidTransactionMap = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Hmily xa transaction manager.
     */
    public HmilyXaTransactionManager() {
    }

    /**
     * Initialized hmily xa transaction manager.
     *
     * @return the hmily xa transaction manager
     */
    public static HmilyXaTransactionManager initialized() {
        return new HmilyXaTransactionManager();
    }

    /**
     * Create transaction transaction.
     *
     * @return the transaction
     * @throws SystemException the system exception
     */
    public Transaction createTransaction() throws SystemException {
        Transaction threadTransaction = getThreadTransaction();
        Transaction rct = threadTransaction;
        //xa;
        if (threadTransaction != null) {
            synchronized (this) {
                if (xidTransactionMap.containsValue(rct)) {
                    if (!txCanRollback(rct)) {
                        throw new RuntimeException(" Nested transactions not supported ");
                    }
                }
            }
        }
        XIdImpl xId = new XIdImpl();
        //Main business coordinator
        rct = new TransactionImpl(xId);
        setTxTotr(rct);
        xidTransactionMap.put(xId, rct);
        return rct;
    }

    /**
     * tx  to threadLocal.
     */
    private void setTxTotr(final Transaction transaction) {
        tms.set(transaction);
    }

    /**
     * Gets transaction.
     *
     * @return the transaction
     */
    public Transaction getTransaction() {
        Transaction transaction = getThreadTransaction();
        if (transaction == null) {
            logger.warn("transaction is null");
        }
        return transaction;
    }

    /**
     * Gets thread transaction.
     *
     * @return the thread transaction
     */
    public Transaction getThreadTransaction() {
        return tms.get();
    }

    /**
     * Rollback transaction.
     *
     * @return the transaction
     */
    public Transaction rollback() {
        Transaction threadTransaction = getThreadTransaction();
        if (threadTransaction != null) {
            tms.remove();
        }
        return threadTransaction;
    }

    /**
     * Commit transaction.
     *
     * @return the transaction
     */
    public Transaction commit() {
        Transaction threadTransaction = getThreadTransaction();
        if (threadTransaction == null) {
            throw new IllegalStateException("Transaction is null,can not commit");
        }
        tms.remove();
        return threadTransaction;
    }

    private boolean txCanRollback(final Transaction transaction) throws SystemException {
        if (transaction.getStatus() == Status
                .STATUS_MARKED_ROLLBACK) {
            transaction.rollback();
            return true;
        }
        return false;
    }

    /**
     * Gets state.
     *
     * @return the state
     * @throws SystemException the system exception
     */
    public Integer getState() throws SystemException {
        Transaction transaction = getTransaction();
        if (transaction == null) {
            return XaState.STATUS_NO_TRANSACTION.getState();
        }
        return transaction.getStatus();
    }
}
