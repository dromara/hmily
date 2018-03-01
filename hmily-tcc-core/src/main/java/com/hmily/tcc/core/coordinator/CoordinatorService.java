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

package com.hmily.tcc.core.coordinator;


import com.hmily.tcc.common.config.TccConfig;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.core.coordinator.command.CoordinatorAction;

/**
 * @author xiaoyu
 */
public interface CoordinatorService {

    /**
     * 启动本地补偿事务，根据配置是否进行补偿
     *
     * @param tccConfig 配置信息
     * @throws Exception 异常
     */
    void start(TccConfig tccConfig) throws Exception;

    /**
     * 保存补偿事务信息
     *
     * @param tccTransaction 实体对象
     * @return 主键id
     */
    String save(TccTransaction tccTransaction);

    /**
     * 根据事务id获取TccTransaction
     *
     * @param transId 事务id
     * @return TccTransaction
     */
    TccTransaction findByTransId(String transId);


    /**
     * 删除补偿事务信息
     *
     * @param id 主键id
     * @return true成功 false 失败
     */
    boolean remove(String id);


    /**
     * 更新
     *
     * @param tccTransaction 实体对象
     */
    void update(TccTransaction tccTransaction);


    /**
     * 更新 List<Participant>  只更新这一个字段数据
     * @param tccTransaction  实体对象
     * @return rows
     */
    int updateParticipant(TccTransaction tccTransaction);


    /**
     * 更新补偿数据状态
     * @param id  事务id
     * @param status  状态
     * @return  rows 1 成功 0 失败
     */
    int updateStatus(String id, Integer status);

    /**
     * 提交补偿操作
     *
     * @param coordinatorAction 执行动作
     * @return true 成功
     */
    Boolean submit(CoordinatorAction coordinatorAction);
}
