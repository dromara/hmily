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

package com.hmily.tcc.dubbo.service;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.hmily.tcc.core.service.RpcApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * DubboRpcApplicationServiceImpl.
 * @author xiaoyu
 */
@Service("applicationService")
public class DubboRpcApplicationServiceImpl implements RpcApplicationService {

    /**
     * dubbo ApplicationConfig.
     */
    private final ApplicationConfig applicationConfig;

    @Autowired(required = false)
    public DubboRpcApplicationServiceImpl(final ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    public String acquireName() {
        return Optional.ofNullable(applicationConfig).orElse(new ApplicationConfig("hmily-dubbo")).getName();
    }
}
