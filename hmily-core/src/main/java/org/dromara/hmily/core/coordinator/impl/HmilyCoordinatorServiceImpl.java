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

package org.dromara.hmily.core.coordinator.impl;

import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.config.HmilyConfig;
import org.dromara.hmily.core.coordinator.HmilyCoordinatorService;
import org.dromara.hmily.core.helper.SpringBeanUtils;
import org.dromara.hmily.core.service.HmilyApplicationService;
import org.dromara.hmily.core.spi.HmilyCoordinatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * impl hmilyCoordinatorService.
 *
 * @author xiaoyu
 */
@Service("hmilyCoordinatorService")
public class HmilyCoordinatorServiceImpl implements HmilyCoordinatorService {

    private HmilyCoordinatorRepository coordinatorRepository;

    private final HmilyApplicationService hmilyApplicationService;

    @Autowired
    public HmilyCoordinatorServiceImpl(final HmilyApplicationService hmilyApplicationService) {
        this.hmilyApplicationService = hmilyApplicationService;
    }

    @Override
    public void start(final HmilyConfig hmilyConfig) {
        final String repositorySuffix = buildRepositorySuffix(hmilyConfig.getRepositorySuffix());
        coordinatorRepository = SpringBeanUtils.getInstance().getBean(HmilyCoordinatorRepository.class);
        coordinatorRepository.init(repositorySuffix, hmilyConfig);
    }

    @Override
    public String save(final HmilyTransaction hmilyTransaction) {
        final int rows = coordinatorRepository.create(hmilyTransaction);
        if (rows > 0) {
            return hmilyTransaction.getTransId();
        }
        return null;
    }

    @Override
    public HmilyTransaction findByTransId(final String transId) {
        return coordinatorRepository.findById(transId);
    }

    @Override
    public boolean remove(final String id) {
        return coordinatorRepository.remove(id) > 0;
    }

    @Override
    public void update(final HmilyTransaction hmilyTransaction) {
        coordinatorRepository.update(hmilyTransaction);
    }

    @Override
    public int updateParticipant(final HmilyTransaction hmilyTransaction) {
        return coordinatorRepository.updateParticipant(hmilyTransaction);
    }

    @Override
    public int updateStatus(final String id, final Integer status) {
        return coordinatorRepository.updateStatus(id, status);
    }

    private String buildRepositorySuffix(final String repositorySuffix) {
        if (StringUtils.isNoneBlank(repositorySuffix)) {
            return repositorySuffix;
        } else {
            return hmilyApplicationService.acquireName();
        }
    }

}
