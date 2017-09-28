/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.happylifeplat.tcc.common.enums;


import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;


/**
 * The enum Blocking queue type enum.
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

    BlockingQueueTypeEnum(String value) {
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
    public static BlockingQueueTypeEnum fromString(String value) {
        Optional<BlockingQueueTypeEnum> blockingQueueTypeEnum =
                Arrays.stream(BlockingQueueTypeEnum.values())
                        .filter(v -> Objects.equals(v.getValue(), value))
                        .findFirst();
        return blockingQueueTypeEnum.orElse(BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE);
    }

    public String toString() {
        return value;
    }
}

