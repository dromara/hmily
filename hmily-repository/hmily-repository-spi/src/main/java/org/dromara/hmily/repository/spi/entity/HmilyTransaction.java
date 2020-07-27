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

package org.dromara.hmily.repository.spi.entity;

import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * The HmilyTransaction.
 *
 * @author xiaoyu
 */
@Data
public class HmilyTransaction implements Serializable {

    private static final long serialVersionUID = -6792063780987394917L;

    /**
     * transaction id.
     */
    private String transId;
    
    /**
     * app name.
     */
    private String appName;

    /**
     * transaction status.
     */
    private int status;
    
    /**
     * trans type.
     */
    private String transType;
    
    /**
     * retry.
     */
    private Integer retry;
    
    /**
     * version number mysql optimistic lock control.
     */
    private Integer version = 1;
    
    /**
     * createTime.
     */
    private Date createTime;
    
    /**
     * updateTime.
     */
    private Date updateTime;

    /**
     * A collection of methods that participate in coordination.
     */
    private List<HmilyParticipant> hmilyParticipants;

    public HmilyTransaction() {
        this.createTime = new Date();
        this.updateTime = new Date();
        hmilyParticipants = Lists.newCopyOnWriteArrayList();

    }

    public HmilyTransaction(final String transId) {
        this.transId = transId;
        this.createTime = new Date();
        this.updateTime = new Date();
        hmilyParticipants = Lists.newCopyOnWriteArrayList();
    }

    /**
     * registerParticipant.
     *
     * @param hmilyParticipant {@linkplain HmilyParticipant}
     */
    public void registerParticipant(final HmilyParticipant hmilyParticipant) {
        hmilyParticipants.add(hmilyParticipant);
    }

}
