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
 * @author xiaoyu
 */

public enum TccActionEnum {


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
