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

package org.dromara.hmily.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The enum Rejected policy type enum.
 *
 * @author xiaoyu
 */
@RequiredArgsConstructor
@Getter
public enum RejectedPolicyTypeEnum {
    /**
     * Abort policy rejected policy type enum.
     */
    ABORT_POLICY("Abort"),
    /**
     * Blocking policy rejected policy type enum.
     */
    BLOCKING_POLICY("Blocking"),
    /**
     * Caller runs policy rejected policy type enum.
     */
    CALLER_RUNS_POLICY("CallerRuns"),
    /**
     * Discarded policy rejected policy type enum.
     */
    DISCARDED_POLICY("Discarded"),
    /**
     * Rejected policy rejected policy type enum.
     */
    REJECTED_POLICY("Rejected");

    private final String value;

    /**
     * From string rejected policy type enum.
     *
     * @param value the value
     * @return the rejected policy type enum
     */
    public static RejectedPolicyTypeEnum fromString(final String value) {
        Optional<RejectedPolicyTypeEnum> rejectedPolicyTypeEnum =
                Arrays.stream(RejectedPolicyTypeEnum.values())
                        .filter(v -> Objects.equals(v.getValue(), value))
                        .findFirst();
        return rejectedPolicyTypeEnum.orElse(RejectedPolicyTypeEnum.ABORT_POLICY);
    }
}

