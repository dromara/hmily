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

/**
 * ModifyEventConsumer .
 * modify config event push .
 *
 * @param <T> the type parameter
 * @author sixh chenbin
 */
public interface EventConsumer<T extends EventData> {

    /**
     * Accept.
     *
     * @param t the t
     */
    void accept(T t);

    /**
     * listener properties Regular expression.
     *
     * @return the string
     */
    String properties();
}
