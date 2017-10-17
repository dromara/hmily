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
    PROVIDER(3, "提供者");


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
