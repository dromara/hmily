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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The enum Coordinator action enum.
 *
 * @author xiaoyu
 */
@RequiredArgsConstructor
@Getter
public enum CoordinatorActionEnum {

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
    UPDATE(2, "更新"),

    /**
     * Rollback coordinator action enum.
     */
    ROLLBACK(3, "回滚"),

    /**
     * Compensation coordinator action enum.
     */
    COMPENSATION(4, "补偿");

    private final int code;

    private final String desc;

}
