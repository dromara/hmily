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

import javax.sql.DataSource;
import javax.transaction.Transaction;

/**
 * HmilyXaTransactionManager .
 *
 * @author sixh chenbin
 */
public class HmilyXaTransactionManager {

    private final Logger logger = LoggerFactory.getLogger(HmilyXaTransactionManager.class);

    private final ThreadLocal<Transaction> tms = new ThreadLocal<>();
    /**
     * Initialized hmily xa transaction manager.
     *
     * @param dataSource the data source
     * @return the hmily xa transaction manager
     */
    public static HmilyXaTransactionManager initialized() {
        return new HmilyXaTransactionManager();
    }
    /**
     * Create transaction transaction.
     *
     * @return the transaction
     */
    public Transaction createTransaction() {
        Transaction threadTransaction = getThreadTransaction();
        Transaction rct = threadTransaction;
        XIdImpl xId = new XIdImpl();
        if (threadTransaction == null) {
            rct = new TransactionImpl(xId);
        } else {

        }
        setTxTotr(rct);
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
            tms.set(null);
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
        tms.set(null);
        return threadTransaction;
    }
}
