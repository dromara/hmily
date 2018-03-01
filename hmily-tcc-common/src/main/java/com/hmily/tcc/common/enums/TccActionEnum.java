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
 * The enum Tcc action enum.
 *
 * @author xiaoyu
 */
public enum TccActionEnum {


    /**
     * Pre try tcc action enum.
     */
    PRE_TRY(0,"开始执行try"),


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


    private int code;

    private String desc;

    TccActionEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    /**
     * Acquire by code tcc action enum.
     *
     * @param code the code
     * @return the tcc action enum
     */
    public static TccActionEnum acquireByCode(int code) {
        Optional<TccActionEnum> tccActionEnum =
                Arrays.stream(TccActionEnum.values())
                        .filter(v -> Objects.equals(v.getCode(), code))
                        .findFirst();
        return tccActionEnum.orElse(TccActionEnum.TRYING);

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
