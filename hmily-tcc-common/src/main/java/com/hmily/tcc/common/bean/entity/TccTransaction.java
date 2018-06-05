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

package com.hmily.tcc.common.bean.entity;

import com.google.common.collect.Lists;
import com.hmily.tcc.common.utils.IdWorkerUtils;
import com.hmily.tcc.common.enums.TccActionEnum;
import com.hmily.tcc.common.enums.TccRoleEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * TccTransaction 实体日志对象.
 * @author xiaoyu
 */
@Data
public class TccTransaction implements Serializable {

    private static final long serialVersionUID = -6792063780987394917L;

    /**
     * 事务id.
     */
    private String transId;

    /**
     * 事务状态.
     * {@linkplain TccActionEnum}
     */
    private int status;

    /**
     * 事务类型.
     * {@linkplain TccRoleEnum}
     */
    private int role;

    /**
     * 重试次数.
     */
    private volatile int retriedCount;

    /**
     * 创建时间.
     */
    private Date createTime;

    /**
     * 更新时间.
     */
    private Date lastTime;

    /**
     * 版本号 乐观锁控制.
     */
    private Integer version = 1;

    /**
     * 模式.
     */
    private Integer pattern;


    /**
     * 调用接口名称.
     */
    private String targetClass;


    /**
     * 调用方法名称.
     */
    private String targetMethod;

    /**
     * 参与协调的方法集合.
     */
    private List<Participant> participants;

    public TccTransaction() {
        this.transId = IdWorkerUtils.getInstance().createUUID();
        this.createTime = new Date();
        this.lastTime = new Date();
        participants = Lists.newCopyOnWriteArrayList();

    }

    public TccTransaction(final String transId) {
        this.transId = transId;
        this.createTime = new Date();
        this.lastTime = new Date();
        participants = Lists.newCopyOnWriteArrayList();
    }

    /**
     * 保存参与者.
     * @param participant 参与者对象
     */
    public void registerParticipant(final Participant participant) {
        participants.add(participant);
    }

}
