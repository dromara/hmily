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
import javax.sql.XAConnection;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.sql.SQLException;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HmilyXaTransactionManager .
 *
 * @author sixh chenbin
 */
public class HmilyXaTransactionManager {

    private Logger logger = LoggerFactory.getLogger(HmilyXaTransactionManager.class);

    private final Map<Thread, Stack<Transaction>> threadStackMap;

    private final DataSource dataSource;

    public HmilyXaTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
        threadStackMap = new ConcurrentHashMap<>(16);
    }

    public static HmilyXaTransactionManager initialized(DataSource dataSource) {
        return new HmilyXaTransactionManager(dataSource);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Transaction createTransaction() {
        Transaction threadTransaction = getThreadTransaction();
        Transaction rct = threadTransaction;
        if (threadTransaction == null) {
            XAConnection connection = null;
            try {
                connection = (XAConnection) dataSource.getConnection();
            } catch (SQLException throwables) {
                throw new RuntimeException(throwables);
            }
            rct = new TransactionImpl(connection, new HmliyTransactionImpl());
        } else {

        }
        addToMap(rct);
        return rct;
    }

    private void addToMap(Transaction rct) {
        Thread thread = Thread.currentThread();
        synchronized (threadStackMap) {
            Stack<Transaction> stack = threadStackMap.get(thread);
            if (stack == null) {
                stack = new Stack<>();
            }
            try {
                if (rct.getStatus() == XaState.STATUS_ACTIVE.getState()) {
                    stack.push(rct);
                }
            } catch (SystemException e) {
            }
        }
    }

    public Transaction getTransaction() {
        Transaction transaction = getThreadTransaction();
        if (transaction == null) {
            logger.warn("transaction is null");
        }
        return transaction;
    }

    public Transaction getThreadTransaction() {
        Thread thread = Thread.currentThread();
        synchronized (threadStackMap) {
            Stack<Transaction> stack = threadStackMap.get(thread);
            if (stack == null) {
                return null;
            }
            return stack.peek();
        }
    }
}
