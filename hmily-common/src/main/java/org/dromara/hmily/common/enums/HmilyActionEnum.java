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

/**
 * The enum hmily action enum.
 *
 * @author xiaoyu
 */
@RequiredArgsConstructor
@Getter
public enum HmilyActionEnum {

    /**
     * Pre try tcc action enum.
     */
    PRE_TRY(0, "开始执行try"),

    /**
     * Trying tcc action enum.
     */
    TRYING(1, "try阶段完成"),

    /**
     * Confirming tcc action enum.
     */
    CONFIRMING(2, "confirm阶段"),

    /**
     * Canceling tcc action enum.
     */
    CANCELING(3, "cancel阶段");

    private final int code;

    private final String desc;

    /**
     * Acquire by code tcc action enum.
     *
     * @param code the code
     * @return the tcc action enum
     */
    public static HmilyActionEnum acquireByCode(final int code) {
        return Arrays.stream(HmilyActionEnum.values())
                        .filter(v -> Objects.equals(v.getCode(), code))
                        .findFirst().orElse(HmilyActionEnum.TRYING);
    }

}
