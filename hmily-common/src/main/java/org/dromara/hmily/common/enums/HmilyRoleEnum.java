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

/**
 * The enum Hmily role enum.
 *
 * @author xiaoyu
 */
@RequiredArgsConstructor
@Getter
public enum HmilyRoleEnum {

    /**
     * Start tcc role enum.
     */
    START(1, "发起者"),

    /**
     * Consumer tcc role enum.
     */
    CONSUMER(2, "消费者"),

    /**
     * Provider tcc role enum.
     */
    PROVIDER(3, "提供者"),

    /**
     * Local tcc role enum.
     */
    LOCAL(4, "本地调用"),

    /**
     * Inline tcc role enum.
     */
    INLINE(5, "内嵌RPC调用"),

    /**
     * Spring cloud tcc role enum.
     */
    SPRING_CLOUD(6, "SpringCloud");

    private final int code;

    private final String desc;

}
