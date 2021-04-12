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

    private final Coordinator coordinator;

    private final Map<XIdImpl, Transaction> xidTransactionMap = new ConcurrentHashMap<>();

    /**
     * Initialized hmily xa transaction manager.
     *
     * @return the hmily xa transaction manager
     */
    public static HmilyXaTransactionManager initialized() {
        return new HmilyXaTransactionManager();
    }

    public HmilyXaTransactionManager() {
        XIdImpl xId = new XIdImpl();
        //Main business coordinator
        coordinator = new Coordinator(xId,null);
    }

    public Coordinator getCoordinator() {
        return coordinator;
    }

    /**
     * Create transaction transaction.
     *
     * @return the transaction
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
        XIdImpl subXid = this.coordinator.getSubXid();
        rct = new TransactionImpl(subXid);
        setTxTotr(rct);
        xidTransactionMap.put(subXid, rct);
        return rct;
    }

    /**
     * tx  to threadLocal;
     */
    private void setTxTotr(Transaction transaction) {
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

    private boolean txCanRollback(Transaction transaction) throws SystemException {
        if (transaction.getStatus() == Status
                .STATUS_MARKED_ROLLBACK) {
            transaction.rollback();
            return true;
        }
        return false;
    }
}
