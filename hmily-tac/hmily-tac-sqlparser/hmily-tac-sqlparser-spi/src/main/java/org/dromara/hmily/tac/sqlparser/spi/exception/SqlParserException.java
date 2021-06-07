/*
 * Copyright 2017-2021 Dromara.org

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

package org.dromara.hmily.tac.sqlparser.spi.exception;

/**
 * The type Sql parser exception.
 *
 * @author xiaoyu
 */
public class SqlParserException extends RuntimeException {
    
    /**
     * Instantiates a new Sql parser exception.
     *
     * @param e the e
     */
    public SqlParserException(final Throwable e) {
        super(e);
    }
    
    /**
     * Instantiates a new Sql parser exception.
     *
     * @param message the message
     */
    public SqlParserException(final String message) {
        super(message);
    }
    
    /**
     * Instantiates a new Sql parser exception.
     *
     * @param message   the message
     * @param throwable the throwable
     */
    public SqlParserException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
