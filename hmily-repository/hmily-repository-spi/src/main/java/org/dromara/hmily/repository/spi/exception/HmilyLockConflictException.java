/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.repository.spi.exception;

/**
 * The type Hmily lock conflict exception.
 *
 * @author zhaojun
 */
public class HmilyLockConflictException extends RuntimeException {
    
    private static final long serialVersionUID = 54809856411203804L;
    
    /**
     * Instantiates a new Hmily lock conflict exception.
     */
    public HmilyLockConflictException() {
    }
    
    /**
     * Instantiates a new Hmily lock conflict exception.
     *
     * @param message the message
     */
    public HmilyLockConflictException(final String message) {
        super(message);
    }
    
    /**
     * Instantiates a new Hmily lock conflict exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public HmilyLockConflictException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Instantiates a new Hmily lock conflict exception.
     *
     * @param cause the cause
     */
    public HmilyLockConflictException(final Throwable cause) {
        super(cause);
    }
}
