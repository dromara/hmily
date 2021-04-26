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

import java.time.LocalDateTime;

/**
 * HmilyXaRecovery .
 *
 * @author sixh chenbin
 */
public interface HmilyXaRecovery {
    /**
     * Gets id.
     *
     * @return the id
     */
    Long getId();

    /**
     * Gets tm unique.
     *
     * @return the tm unique
     */
    String getTmUnique();

    /**
     * Gets global id.
     *
     * @return the global id
     */
    String getGlobalId();

    /**
     * Gets branch id.
     *
     * @return the branch id
     */
    String getBranchId();

    /**
     * Gets end xid.
     *
     * @return the end xid
     */
    Long getEndXid();

    /**
     * Gets end bxid.
     *
     * @return the end bxid
     */
    Long getEndBxid();

    /**
     * Gets super id.
     *
     * @return the super id
     */
    String getSuperId();

    /**
     * Gets is coordinator.
     *
     * @return the is coordinator
     */
    Boolean getIsCoordinator();

    /**
     * Gets url.
     *
     * @return the url
     */
    String getUrl();

    /**
     * Gets state.
     *
     * @return the state
     */
    Integer getState();

    /**
     * Gets create time.
     *
     * @return the create time
     */
    LocalDateTime getCreateTime();

    /**
     * Gets update time.
     *
     * @return the update time
     */
    LocalDateTime getUpdateTime();

    /**
     * Gets version.
     *
     * @return the version
     */
    Integer getVersion();
}
