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

package com.hmily.tcc.core.coordinator.impl;


import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.config.TccConfig;
import com.hmily.tcc.core.coordinator.CoordinatorService;
import com.hmily.tcc.core.coordinator.command.CoordinatorAction;
import com.hmily.tcc.core.helper.SpringBeanUtils;
import com.hmily.tcc.core.schedule.ScheduledService;
import com.hmily.tcc.core.service.ApplicationService;
import com.hmily.tcc.core.spi.CoordinatorRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xiaoyu
 */
@Service("coordinatorService")
public class CoordinatorServiceImpl implements CoordinatorService {

    private CoordinatorRepository coordinatorRepository;

    private final ApplicationService applicationService;

    @Autowired
    public CoordinatorServiceImpl(ApplicationService applicationService) {
        this.applicationService = applicationService;

    }


    /**
     * 初始化协调资源
     *
     * @param tccConfig 配置信息
     * @throws Exception 异常
     */
    @Override
    public void start(TccConfig tccConfig) throws Exception {
        final String repositorySuffix =
                buildRepositorySuffix(tccConfig.getRepositorySuffix());
        coordinatorRepository = SpringBeanUtils.getInstance()
                .getBean(CoordinatorRepository.class);
        //初始化spi 协调资源存储
        coordinatorRepository.init(repositorySuffix, tccConfig);

        new ScheduledService(tccConfig, coordinatorRepository).scheduledRollBack();

    }


    /**
     * 保存补偿事务信息
     *
     * @param tccTransaction 实体对象
     * @return 主键id
     */
    @Override
    public String save(TccTransaction tccTransaction) {
        final int rows = coordinatorRepository.create(tccTransaction);
        if (rows > 0) {
            return tccTransaction.getTransId();
        }
        return null;
    }

    @Override
    public TccTransaction findByTransId(String transId) {
        return coordinatorRepository.findById(transId);
    }

    /**
     * 删除补偿事务信息
     *
     * @param id 主键id
     * @return true成功 false 失败
     */
    @Override
    public boolean remove(String id) {
        return coordinatorRepository.remove(id) > 0;
    }

    /**
     * 更新
     *
     * @param tccTransaction 实体对象
     */
    @Override
    public void update(TccTransaction tccTransaction) {
        coordinatorRepository.update(tccTransaction);
    }

    /**
     * 更新 List<Participant>  只更新这一个字段数据
     *
     * @param tccTransaction 实体对象
     */
    @Override
    public int updateParticipant(TccTransaction tccTransaction) {
        return coordinatorRepository.updateParticipant(tccTransaction);
    }

    /**
     * 更新补偿数据状态
     *
     * @param id     事务id
     * @param status 状态
     * @return rows 1 成功 0 失败
     */
    @Override
    public int updateStatus(String id, Integer status) {
        return coordinatorRepository.updateStatus(id, status);
    }

    /**
     * 提交补偿操作
     *
     * @param coordinatorAction 执行动作
     */
    @Override
    public Boolean submit(CoordinatorAction coordinatorAction) {
        return Boolean.TRUE;
    }

    private String buildRepositorySuffix(String repositorySuffix) {
        if (StringUtils.isNoneBlank(repositorySuffix)) {
            return repositorySuffix;
        } else {
            return applicationService.acquireName();
        }

    }


}
