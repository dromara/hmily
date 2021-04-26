/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dromara.hmily.repository.spi.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * HmilyXaRecovery .
 *
 * @author sixh chenbin
 */
@Data
public class HmilyXaRecoveryImpl implements HmilyXaRecovery, Serializable {

    private Long id;

    private String tmUnique;

    private String globalId;

    private String branchId;

    private Long endXid;

    private Long endBxid;

    private String superId;

    private Boolean isCoordinator;

    private String url;

    private Integer state;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer version;

    public static <T extends HmilyXaRecovery> HmilyXaRecovery convert(T t) {
        HmilyXaRecoveryImpl impl = new HmilyXaRecoveryImpl();
        impl.setCreateTime(t.getCreateTime());
        impl.setUpdateTime(t.getUpdateTime());
        impl.setBranchId(t.getBranchId());
        impl.setEndBxid(t.getEndBxid());
        impl.setEndXid(t.getEndXid());
        impl.setGlobalId(t.getGlobalId());
        impl.setIsCoordinator(t.getIsCoordinator());
        impl.setState(t.getState());
        impl.setSuperId(t.getSuperId());
        impl.setTmUnique(t.getTmUnique());
        impl.setUrl(t.getUrl());
        impl.setVersion(t.getVersion());
        return impl;
    }
}
