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

package com.hmily.tcc.admin.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/19 16:37
 * @since JDK 1.8
 */
@Data
public class TccCompensationVO implements Serializable {


    private static final long serialVersionUID = 564418979137349581L;


    private String transId;


    /**
     * 重试次数，
     */
    private Integer retriedCount;

    /**
     * 创建时间
     */
    private String createTime;


    /**
     * 创建时间
     */
    private String lastTime;

    /**
     * 版本控制 防止并发问题
     */
    private Integer version;


    /**
     * 执行类名称
     */
    private String targetClass;
    /**
     * 执行方法
     */
    private String targetMethod;

    /**
     * 确认方法
     */
    private String confirmMethod;

    /**
     * 取消方法
     */
    private String cancelMethod;


}
