/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.tac.core.exception;

/**
 *The type hmily lock wait timeout exception.
 *
 * @author zhangzhi
 */
public class LockWaitTimeoutException extends RuntimeException {
    
    private static final long serialVersionUID = 54809856411203804L;
    
    /**
     * Instantiates a new Hmily lock wait timeout exception.
     */
    public LockWaitTimeoutException() {
    }
    
    /**
     * Instantiates a new Hmily lock wait timeout exception.
     *
     * @param message the message
     */
    public LockWaitTimeoutException(final String message) {
        super(message);
    }
    
    /**
     * Instantiates a new Hmily lock wait timeout exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public LockWaitTimeoutException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Instantiates a new Hmily lock wait timeout exception.
     *
     * @param cause the cause
     */
    public LockWaitTimeoutException(final Throwable cause) {
        super(cause);
    }
}
