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

package com.hmily.tcc.core.service.impl;


import com.hmily.tcc.common.config.TccConfig;
import com.hmily.tcc.common.enums.RepositorySupportEnum;
import com.hmily.tcc.common.enums.SerializeEnum;
import com.hmily.tcc.common.utils.LogUtil;
import com.hmily.tcc.core.coordinator.CoordinatorService;
import com.hmily.tcc.core.disruptor.publisher.TccTransactionEventPublisher;
import com.hmily.tcc.core.helper.SpringBeanUtils;
import com.hmily.tcc.core.service.TccInitService;
import com.hmily.tcc.core.spi.CoordinatorRepository;
import com.hmily.tcc.common.serializer.ObjectSerializer;
import com.hmily.tcc.common.utils.ServiceBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;


/**
 * @author xiaoyu
 */
@Service("tccInitService")
public class TccInitServiceImpl implements TccInitService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TccInitServiceImpl.class);


    private final CoordinatorService coordinatorService;

    @Autowired
    private TccTransactionEventPublisher tccTransactionEventPublisher;

    @Autowired
    public TccInitServiceImpl(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    /**
     * tcc分布式事务初始化方法
     *
     * @param tccConfig TCC配置
     */
    @Override
    public void initialization(TccConfig tccConfig) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.error("系统关闭")));
        try {
            loadSpiSupport(tccConfig);
            tccTransactionEventPublisher.start(tccConfig.getBufferSize());
            coordinatorService.start(tccConfig);
        } catch (Exception ex) {
            LogUtil.error(LOGGER, "tcc事务初始化异常:{}", ex::getMessage);
            //非正常关闭
            System.exit(1);
        }
        LogUtil.info(LOGGER, () -> "Tcc事务初始化成功！");
    }

    /**
     * 根据配置文件初始化spi
     *
     * @param tccConfig 配置信息
     */
    private void loadSpiSupport(TccConfig tccConfig) {

        //spi  serialize
        final SerializeEnum serializeEnum =
                SerializeEnum.acquire(tccConfig.getSerializer());
        final ServiceLoader<ObjectSerializer> objectSerializers = ServiceBootstrap.loadAll(ObjectSerializer.class);

        final Optional<ObjectSerializer> serializer = StreamSupport.stream(objectSerializers.spliterator(), false)
                .filter(objectSerializer ->
                        Objects.equals(objectSerializer.getScheme(), serializeEnum.getSerialize())).findFirst();


        //spi  repository support
        final RepositorySupportEnum repositorySupportEnum = RepositorySupportEnum.acquire(tccConfig.getRepositorySupport());
        final ServiceLoader<CoordinatorRepository> recoverRepositories = ServiceBootstrap.loadAll(CoordinatorRepository.class);


        final Optional<CoordinatorRepository> repositoryOptional = StreamSupport.stream(recoverRepositories.spliterator(), false)
                .filter(recoverRepository ->
                        Objects.equals(recoverRepository.getScheme(), repositorySupportEnum.getSupport())).findFirst();

        //将CoordinatorRepository实现注入到spring容器
        repositoryOptional.ifPresent(repository -> {
            serializer.ifPresent(repository::setSerializer);
            SpringBeanUtils.getInstance().registerBean(CoordinatorRepository.class.getName(), repository);
        });


    }
}
