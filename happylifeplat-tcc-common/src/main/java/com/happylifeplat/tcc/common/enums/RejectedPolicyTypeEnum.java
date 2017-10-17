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
 * The enum Rejected policy type enum.
 *
 * @author xiaoyu
 */
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

    private String value;

    RejectedPolicyTypeEnum(String value) {
        this.value = value;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * From string rejected policy type enum.
     *
     * @param value the value
     * @return the rejected policy type enum
     */
    public static RejectedPolicyTypeEnum fromString(String value) {
        Optional<RejectedPolicyTypeEnum> rejectedPolicyTypeEnum =
                Arrays.stream(RejectedPolicyTypeEnum.values())
                        .filter(v -> Objects.equals(v.getValue(), value))
                        .findFirst();
        return rejectedPolicyTypeEnum.orElse(RejectedPolicyTypeEnum.ABORT_POLICY);
    }

    @Override
    public String toString() {
        return value;
    }
}

