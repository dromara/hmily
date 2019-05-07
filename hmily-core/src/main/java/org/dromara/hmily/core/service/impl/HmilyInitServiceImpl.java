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

package org.dromara.hmily.core.service.impl;

import org.dromara.hmily.common.config.HmilyConfig;
import org.dromara.hmily.common.serializer.ObjectSerializer;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.utils.extension.ExtensionLoader;
import org.dromara.hmily.core.coordinator.HmilyCoordinatorService;
import org.dromara.hmily.core.helper.SpringBeanUtils;
import org.dromara.hmily.core.logo.HmilyLogo;
import org.dromara.hmily.core.service.HmilyInitService;
import org.dromara.hmily.core.spi.HmilyCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * hmily init service.
 *
 * @author xiaoyu
 */
@Service("hmilyInitService")
public class HmilyInitServiceImpl implements HmilyInitService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyInitServiceImpl.class);

    private final HmilyCoordinatorService hmilyCoordinatorService;

    /**
     * Instantiates a new Hmily init service.
     *
     * @param hmilyCoordinatorService the hmily coordinator service
     */
    @Autowired
    public HmilyInitServiceImpl(final HmilyCoordinatorService hmilyCoordinatorService) {
        this.hmilyCoordinatorService = hmilyCoordinatorService;
    }

    /**
     * hmily initialization.
     *
     * @param hmilyConfig {@linkplain HmilyConfig}
     */
    @Override
    public void initialization(final HmilyConfig hmilyConfig) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.info("hmily shutdown now")));
        try {
            loadSpiSupport(hmilyConfig);
            hmilyCoordinatorService.start(hmilyConfig);
        } catch (Exception ex) {
            LogUtil.error(LOGGER, " hmily init exception:{}", ex::getMessage);
            System.exit(1);
        }
        new HmilyLogo().logo();
    }

    /**
     * load spi.
     *
     * @param hmilyConfig {@linkplain HmilyConfig}
     */
    private void loadSpiSupport(final HmilyConfig hmilyConfig) {
        //spi serialize
        final ObjectSerializer serializer = ExtensionLoader.getExtensionLoader(ObjectSerializer.class)
                .getActivateExtension(hmilyConfig.getSerializer());

        //spi repository
        final HmilyCoordinatorRepository repository = ExtensionLoader.getExtensionLoader(HmilyCoordinatorRepository.class)
                .getActivateExtension(hmilyConfig.getRepositorySupport());

        repository.setSerializer(serializer);

        SpringBeanUtils.getInstance().registerBean(HmilyCoordinatorRepository.class.getName(), repository);
    }
}
