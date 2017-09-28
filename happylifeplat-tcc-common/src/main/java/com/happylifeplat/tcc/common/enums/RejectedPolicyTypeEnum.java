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


public enum RejectedPolicyTypeEnum {
    ABORT_POLICY("Abort"),
    BLOCKING_POLICY("Blocking"),
    CALLER_RUNS_POLICY("CallerRuns"),
    DISCARDED_POLICY("Discarded"),
    REJECTED_POLICY("Rejected");

    private String value;

    RejectedPolicyTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RejectedPolicyTypeEnum fromString(String value) {
        Optional<RejectedPolicyTypeEnum> rejectedPolicyTypeEnum =
                Arrays.stream(RejectedPolicyTypeEnum.values())
                        .filter(v -> Objects.equals(v.getValue(), value))
                        .findFirst();
        return rejectedPolicyTypeEnum.orElse(RejectedPolicyTypeEnum.ABORT_POLICY);
    }

    public String toString() {
        return value;
    }
}

