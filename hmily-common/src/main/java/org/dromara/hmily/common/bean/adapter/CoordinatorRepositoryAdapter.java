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

package org.dromara.hmily.common.bean.adapter;

import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * this is coordinator repository adapter.
 * @author xiaoyu(Myth)
 */
@Data
@NoArgsConstructor
public class CoordinatorRepositoryAdapter {

    /**
     * 事务id.
     */
    private String transId;

    /**
     * 事务状态. {@linkplain HmilyActionEnum}
     */
    private int status;

    /**
     * 事务类型. {@linkplain HmilyRoleEnum}
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
     * 序列化后的二进制信息.
     */
    private byte[] contents;



    /**
     * 调用接口名称.
     */
    private String targetClass;


    /**
     * 调用方法名称.
     */
    private String targetMethod;

    /**
     * confirm方法.
     */
    private String confirmMethod;

    /**
     * cancel方法.
     */
    private String cancelMethod;

}
