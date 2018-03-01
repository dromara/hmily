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
 * The enum Tcc role enum.
 *
 * @author xiaoyu
 */
public enum TccRoleEnum {


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
    LOCAL(4,"本地调用");


    private int code;

    private String desc;

    TccRoleEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    /**
     * Acquire by code tcc action enum.
     *
     * @param code the code
     * @return the tcc action enum
     */
    public static TccRoleEnum acquireByCode(int code) {
        Optional<TccRoleEnum> tccRoleEnum =
                Arrays.stream(TccRoleEnum.values())
                        .filter(v -> Objects.equals(v.getCode(), code))
                        .findFirst();
        return tccRoleEnum.orElse(TccRoleEnum.START);

    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Sets code.
     *
     * @param code the code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Gets desc.
     *
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Sets desc.
     *
     * @param desc the desc
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
