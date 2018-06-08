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
import com.hmily.tcc.core.helper.SpringBeanUtils;
import com.hmily.tcc.core.schedule.ScheduledService;
import com.hmily.tcc.core.service.RpcApplicationService;
import com.hmily.tcc.core.spi.CoordinatorRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * CoordinatorServiceImpl.
 * @author xiaoyu
 */
@Service("coordinatorService")
public class CoordinatorServiceImpl implements CoordinatorService {

    private CoordinatorRepository coordinatorRepository;

    private final RpcApplicationService rpcApplicationService;

    @Autowired
    public CoordinatorServiceImpl(final RpcApplicationService rpcApplicationService) {
        this.rpcApplicationService = rpcApplicationService;
    }

    @Override
    public void start(final TccConfig tccConfig) {
        final String repositorySuffix = buildRepositorySuffix(tccConfig.getRepositorySuffix());
        coordinatorRepository = SpringBeanUtils.getInstance().getBean(CoordinatorRepository.class);
        coordinatorRepository.init(repositorySuffix, tccConfig);
        new ScheduledService(tccConfig, coordinatorRepository).scheduledRollBack();
    }

    @Override
    public String save(final TccTransaction tccTransaction) {
        final int rows = coordinatorRepository.create(tccTransaction);
        if (rows > 0) {
            return tccTransaction.getTransId();
        }
        return null;
    }

    @Override
    public TccTransaction findByTransId(final String transId) {
        return coordinatorRepository.findById(transId);
    }

    @Override
    public boolean remove(final String id) {
        return coordinatorRepository.remove(id) > 0;
    }

    @Override
    public void update(final TccTransaction tccTransaction) {
        coordinatorRepository.update(tccTransaction);
    }

    @Override
    public int updateParticipant(final TccTransaction tccTransaction) {
        return coordinatorRepository.updateParticipant(tccTransaction);
    }

    @Override
    public int updateStatus(final String id, final Integer status) {
        return coordinatorRepository.updateStatus(id, status);
    }

    private String buildRepositorySuffix(final String repositorySuffix) {
        if (StringUtils.isNoneBlank(repositorySuffix)) {
            return repositorySuffix;
        } else {
            return rpcApplicationService.acquireName();
        }
    }

}
