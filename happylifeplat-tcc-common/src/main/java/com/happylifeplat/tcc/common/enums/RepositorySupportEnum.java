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
public enum RepositorySupportEnum {

    /**
     * Db compensate cache type enum.
     */
    DB("db"),

    /**
     * File compensate cache type enum.
     */
    FILE("file"),

    /**
     * Redis compensate cache type enum.
     */
    REDIS("redis"),

    /**
     * Mongodb compensate cache type enum.
     */
    MONGODB("mongodb"),

    /**
     * Zookeeper compensate cache type enum.
     */
    ZOOKEEPER("zookeeper");

    private String support;

    RepositorySupportEnum(String support) {
        this.support = support;
    }

    /**
     * Acquire compensate cache type compensate cache type enum.
     *
     * @param support the compensate cache type
     * @return the compensate cache type enum
     */
    public static RepositorySupportEnum acquire(String support) {
        Optional<RepositorySupportEnum> repositorySupportEnum =
                Arrays.stream(RepositorySupportEnum.values())
                        .filter(v -> Objects.equals(v.getSupport(), support))
                        .findFirst();
        return repositorySupportEnum.orElse(RepositorySupportEnum.DB);
    }


    public String getSupport() {
        return support;
    }

    public void setSupport(String support) {
        this.support = support;
    }
}
