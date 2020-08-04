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
    
    CREATE_HMILY_TRANSACTION(10, "创建全局事务"),
    
    UPDATE_HMILY_TRANSACTION_STATUS(11, "更新全局事务状态"),
    
    REMOVE_HMILY_TRANSACTION(12, "删除全局日志"),
    
    CREATE_HMILY_PARTICIPANT(20, "创建参与者"),
    
    UPDATE_HMILY_PARTICIPANT_STATUS(21, "更新参与者状态"),
    
    REMOVE_HMILY_PARTICIPANT(22, "删除参与者日志"),
    
    REMOVE_HMILY_PARTICIPANT_UNDO(30, "删除undo日志"),
    
    /**
     * Delete coordinator action enum.
     */
    DELETE(20, "删除"),

    /**
     * Update coordinator action enum.
     */
    UPDATE_STATUS(30, "更新状态"),

    /**
     * Rollback coordinator action enum.
     */
    UPDATE_PARTICIPANT(40, "更新参与者");

    private final int code;

    private final String desc;

    /**
     * Build by code event type enum.
     *
     * @param code the code
     * @return the event type enum
     */
    public static EventTypeEnum buildByCode(final int code) {
        return Arrays.stream(EventTypeEnum.values()).filter(e -> e.code == code).findFirst()
                .orElseThrow(() -> new HmilyRuntimeException("can not support this code!"));
    }

}
