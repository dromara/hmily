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

import javax.transaction.xa.XAException;
import java.util.HashMap;
import java.util.Map;

/**
 * HmliyXaException .
 *
 * @author sixh chenbin
 */
public class HmliyXaException extends XAException {

    private static final Map<Integer, String> errorCodes = new HashMap<>();

    public static final int UNKNOWN = 1000;

    /**
     * Instantiates a new Hmliy xa exception.
     *
     * @param errorCode the error code
     */
    public HmliyXaException(final int errorCode) {
        super(errorCode);
    }

    static {
        //XA_RBBASE,XA_RBROLLBACK
        errorCodes.put(XAException.XA_RBROLLBACK, "the XaResource indicates that the rollback was caused by an unspecified reason.");
        errorCodes.put(XAException.XA_RBCOMMFAIL, "the XaResource indicates that the rollback was caused by a communication failure.");
        errorCodes.put(XAException.XA_RBDEADLOCK, "the XaResource a deadlock was detected.");
        errorCodes.put(XAException.XA_RBINTEGRITY, "the XaResource a condition that violates the integrity of the resource was detected.");
        errorCodes.put(XAException.XA_RBOTHER, "the XaResource the resource manager rolled back the transaction branch for a reason not on this list.");
        errorCodes.put(XAException.XA_RBPROTO, "the XaResource a protocol error occurred in the resource manager.");
        errorCodes.put(XAException.XA_RBTIMEOUT, "the XaResource a transaction branch took too long.");
        //XA_RBTRANSIENT,XA_RBEND
        errorCodes.put(XAException.XA_RBTRANSIENT, "the XaResource may retry the transaction branch.");
        errorCodes.put(XAException.XA_NOMIGRATE, "the XaResource resumption must occur where the suspension occurred.");

        errorCodes.put(XAException.XA_HEURHAZ, "the XaResource the transaction branch may have been heuristically completed");
        errorCodes.put(XAException.XA_HEURCOM, "the XaResource the transaction branch has been heuristically committed.");
        errorCodes.put(XAException.XA_HEURRB, "the XaResource the transaction branch has been heuristically rolled back.");
        errorCodes.put(XAException.XA_HEURMIX, "the XaResource the transaction branch has been heuristically committed and rolled back.");

        errorCodes.put(XAException.XA_RETRY, "the XaResource routine returned with no effect and may be reissued.");
        errorCodes.put(XAException.XA_RDONLY, "the XaResource the transaction branch was read-only and has been committed.");
        errorCodes.put(XAException.XAER_ASYNC, "the XaResource there is an asynchronous operation already outstanding.");
        errorCodes.put(XAException.XAER_RMERR, "the XaResource a resource manager error has occurred in the transaction branch.");
        errorCodes.put(XAException.XAER_NOTA, "the XaResource the XID is not valid.");
        errorCodes.put(XAException.XAER_INVAL, "the XaResource invalid arguments were given.");
        errorCodes.put(XAException.XAER_PROTO, "the XaResource routine was invoked in an inproper context.");
        errorCodes.put(XAException.XAER_RMFAIL, "the XaResource resource manager is unavailable.");
        errorCodes.put(XAException.XAER_DUPID, "the XaResource the XID already exists.");
        errorCodes.put(XAException.XAER_OUTSIDE, "the XaResource the resource manager is doing work outside a global transaction.");
    }

    /**
     * Gets message.
     *
     * @param xaException the xa exception
     * @return the message
     */
    static String getMessage(XAException xaException) {
        int errorCode = xaException.errorCode;
        String s = errorCodes.get(errorCode);
        return "errorCode:" + errorCode + ":" + s;
    }
}
