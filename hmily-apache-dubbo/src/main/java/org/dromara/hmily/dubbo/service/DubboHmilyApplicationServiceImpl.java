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

package org.dromara.hmily.dubbo.service;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.commons.lang3.RandomUtils;
import org.dromara.hmily.core.service.HmilyApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * The dubbo impl HmilyApplicationService.
 *
 * @author xiaoyu
 */
@Service("hmilyApplicationService")
public class DubboHmilyApplicationServiceImpl implements HmilyApplicationService {

    private static final String DEFAULT_APPLICATION_NAME = "hmilyApacheDubbo";

    /**
     * dubbo ApplicationConfig.
     */
    private final ApplicationConfig applicationConfig;

    @Autowired(required = false)
    public DubboHmilyApplicationServiceImpl(final ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    public String acquireName() {
        return Optional.ofNullable(applicationConfig).orElse(new ApplicationConfig(buildDefaultApplicationName())).getName();
    }

    private String buildDefaultApplicationName() {
        return DEFAULT_APPLICATION_NAME + RandomUtils.nextInt(1, 10);
    }

}
