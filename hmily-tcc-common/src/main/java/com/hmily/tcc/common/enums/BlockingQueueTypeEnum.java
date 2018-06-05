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

package com.hmily.tcc.common.enums;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The enum Blocking queue type enum.
 *
 * @author xiaoyu
 */
public enum BlockingQueueTypeEnum {

    /**
     * Linked blocking queue blocking queue type enum.
     */
    LINKED_BLOCKING_QUEUE("Linked"),
    /**
     * Array blocking queue blocking queue type enum.
     */
    ARRAY_BLOCKING_QUEUE("Array"),
    /**
     * Synchronous queue blocking queue type enum.
     */
    SYNCHRONOUS_QUEUE("SynchronousQueue");

    private String value;

    BlockingQueueTypeEnum(final String value) {
        this.value = value;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * From string blocking queue type enum.
     *
     * @param value the value
     * @return the blocking queue type enum
     */
    public static BlockingQueueTypeEnum fromString(final String value) {
        Optional<BlockingQueueTypeEnum> blockingQueueTypeEnum =
                Arrays.stream(BlockingQueueTypeEnum.values())
                        .filter(v -> Objects.equals(v.getValue(), value))
                        .findFirst();
        return blockingQueueTypeEnum.orElse(BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE);
    }

    @Override
    public String toString() {
        return value;
    }
}

