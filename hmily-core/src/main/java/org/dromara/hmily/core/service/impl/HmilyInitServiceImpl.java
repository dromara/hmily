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
import org.dromara.hmily.common.enums.RepositorySupportEnum;
import org.dromara.hmily.common.enums.SerializeEnum;
import org.dromara.hmily.common.serializer.KryoSerializer;
import org.dromara.hmily.common.serializer.ObjectSerializer;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.utils.ServiceBootstrap;
import org.dromara.hmily.core.coordinator.HmilyCoordinatorService;
import org.dromara.hmily.core.disruptor.publisher.HmilyTransactionEventPublisher;
import org.dromara.hmily.core.helper.SpringBeanUtils;
import org.dromara.hmily.core.logo.HmilyLogo;
import org.dromara.hmily.core.service.HmilyInitService;
import org.dromara.hmily.core.spi.HmilyCoordinatorRepository;
import org.dromara.hmily.core.spi.repository.JdbcCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

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

    private final HmilyTransactionEventPublisher hmilyTransactionEventPublisher;

    @Autowired
    public HmilyInitServiceImpl(final HmilyCoordinatorService hmilyCoordinatorService, final HmilyTransactionEventPublisher hmilyTransactionEventPublisher) {
        this.hmilyCoordinatorService = hmilyCoordinatorService;
        this.hmilyTransactionEventPublisher = hmilyTransactionEventPublisher;
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
            hmilyTransactionEventPublisher.start(hmilyConfig.getBufferSize(), hmilyConfig.getConsumerThreads());
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
        final SerializeEnum serializeEnum = SerializeEnum.acquire(hmilyConfig.getSerializer());
        final ServiceLoader<ObjectSerializer> objectSerializers = ServiceBootstrap.loadAll(ObjectSerializer.class);
        final ObjectSerializer serializer = StreamSupport.stream(objectSerializers.spliterator(), false)
                .filter(objectSerializer -> Objects.equals(objectSerializer.getScheme(), serializeEnum.getSerialize()))
                .findFirst().orElse(new KryoSerializer());
        //spi repository
        final RepositorySupportEnum repositorySupportEnum = RepositorySupportEnum.acquire(hmilyConfig.getRepositorySupport());
        final ServiceLoader<HmilyCoordinatorRepository> recoverRepositories = ServiceBootstrap.loadAll(HmilyCoordinatorRepository.class);
        final HmilyCoordinatorRepository repository = StreamSupport.stream(recoverRepositories.spliterator(), false)
                .filter(recoverRepository -> Objects.equals(recoverRepository.getScheme(), repositorySupportEnum.getSupport()))
                .findFirst().orElse(new JdbcCoordinatorRepository());
        repository.setSerializer(serializer);
        SpringBeanUtils.getInstance().registerBean(HmilyCoordinatorRepository.class.getName(), repository);
    }
}
