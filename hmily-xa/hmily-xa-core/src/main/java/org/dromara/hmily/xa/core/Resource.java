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

import javax.transaction.TransactionRolledbackException;
import java.rmi.RemoteException;

/**
 * Mock .
 *
 * @author sixh chenbin
 */
public interface Resource {

    /**
     * 2pc phase 1.
     *
     * @return int int
     */
    Result prepare();

    /**
     * Rollback.
     */
    void rollback();

    /**
     * Commit.
     *
     * @throws TransactionRolledbackException the transaction rolledback exception
     * @throws RemoteException                the remote exception
     */
    void commit() throws TransactionRolledbackException, RemoteException;

    /**
     * 1 pc .
     *
     * @throws RemoteException the remote exception
     */
    void onePhaseCommit() throws RemoteException;

    /**
     * The enum Result.
     */
    enum Result {
        /**
         * Commit result.
         */
        COMMIT,
        /**
         * Rollback result.
         */
        ROLLBACK,
        /**
         * Readonly result.
         */
        READONLY;

        /**
         * Gets result.
         *
         * @param r the r
         * @return the result
         */
        public static Result getResult(final int r) {
            Result rs = READONLY;
            switch (r) {
                case 0:
                    rs = COMMIT;
                    break;
                case 1:
                    rs = ROLLBACK;
                    break;
                case 2:
                    rs = READONLY;
                    break;
                default:
                    break;
            }
            return rs;
        }
    }
}
