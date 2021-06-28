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

import javax.transaction.Status;
import java.util.Arrays;

/**
 * XaState .
 *
 * @author sixh chenbin
 */
public enum XaState {

    /**
     * Status active xa state.
     */
    STATUS_ACTIVE(Status.STATUS_ACTIVE),

    /**
     * Status marked rollback xa state.
     */
    STATUS_MARKED_ROLLBACK(Status.STATUS_MARKED_ROLLBACK),

    /**
     * Status prepared xa state.
     */
    STATUS_PREPARED(Status.STATUS_PREPARED),

    /**
     * Status committed xa state.
     */
    STATUS_COMMITTED(Status.STATUS_COMMITTED),

    /**
     * Status rolledback xa state.
     */
    STATUS_ROLLEDBACK(Status.STATUS_ROLLEDBACK),

    /**
     * Status unknown xa state.
     */
    STATUS_UNKNOWN(Status.STATUS_UNKNOWN),

    /**
     * Status no transaction xa state.
     */
    STATUS_NO_TRANSACTION(Status.STATUS_NO_TRANSACTION),

    /**
     * Status preparing xa state.
     */
    STATUS_PREPARING(Status.STATUS_PREPARING),

    /**
     * Status committing xa state.
     */
    STATUS_COMMITTING(Status.STATUS_COMMITTING),

    /**
     * Status rolling back xa state.
     */
    STATUS_ROLLING_BACK(Status.STATUS_ROLLING_BACK);

    private final Integer state;

    XaState(final Integer state) {
        this.state = state;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public Integer getState() {
        return state;
    }

    /**
     * Value of xa state.
     *
     * @param state the state
     * @return the xa state
     */
    public static XaState valueOf(final int state) {
        return Arrays.stream(XaState.values()).filter(e -> e.getState() == state).findFirst().orElse(STATUS_UNKNOWN);
    }
}
