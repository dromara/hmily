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

package org.dromara.hmily.config.api.event;

import java.util.function.Consumer;

/**
 * ChangeEvent .
 * Notify event changes. When the configuration file changes, the processed event will be notified.
 *
 * @author sixh chenbin
 */
public enum ChangeEvent {
    /**
     * Add change event.
     */
    ADD {
        @Override
        public boolean match(Consumer<?> consumer) {
            return false;
        }
    },

    /**
     * Update change event.
     */
    MODIFY {
        @Override
        public boolean match(Consumer<?> consumer) {
            return consumer instanceof ModifyEventConsumer;
        }
    },

    /**
     * Remove change event.
     */
    REMOVE() {
        @Override
        public boolean match(Consumer<?> consumer) {
            return false;
        }
    },
    ;

    /**
     * Match the result of processing class.
     *
     * @param consumer the consumer
     * @return boolean
     */
    public abstract boolean match(Consumer<?> consumer);
}
