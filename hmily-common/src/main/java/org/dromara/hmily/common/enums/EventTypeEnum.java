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
import org.dromara.hmily.common.exception.HmilyRuntimeException;

import java.util.Arrays;

/**
 * The enum Coordinator action enum.
 *
 * @author xiaoyu
 */
@RequiredArgsConstructor
@Getter
public enum EventTypeEnum {

    /**
     * Save coordinator action enum.
     */
    SAVE(0, "保存"),

    /**
     * Delete coordinator action enum.
     */
    DELETE(1, "删除"),

    /**
     * Update coordinator action enum.
     */
    UPDATE_STATUS(2, "更新状态"),

    /**
     * Rollback coordinator action enum.
     */
    UPDATE_PARTICIPANT(3, "更新参与者");

    private final int code;

    private final String desc;

    /**
     * Build by code event type enum.
     *
     * @param code the code
     * @return the event type enum
     */
    public static EventTypeEnum buildByCode(int code) {
        return Arrays.stream(EventTypeEnum.values()).filter(e -> e.code == code).findFirst()
                .orElseThrow(() -> new HmilyRuntimeException("can not support this code!"));
    }

}
