/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.happylifeplat.tcc.core.service.impl;


import com.happylifeplat.tcc.common.config.TccConfig;
import com.happylifeplat.tcc.common.enums.RepositorySupportEnum;
import com.happylifeplat.tcc.common.enums.SerializeEnum;
import com.happylifeplat.tcc.common.utils.LogUtil;
import com.happylifeplat.tcc.core.coordinator.CoordinatorService;
import com.happylifeplat.tcc.core.helper.SpringBeanUtils;
import com.happylifeplat.tcc.core.service.TccInitService;
import com.happylifeplat.tcc.core.spi.CoordinatorRepository;
import com.happylifeplat.tcc.core.spi.ObjectSerializer;
import com.happylifeplat.tcc.core.spi.ServiceBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;


@Service("tccInitService")
public class TccInitServiceImpl implements TccInitService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TccInitServiceImpl.class);


    private final CoordinatorService coordinatorService;

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
    	//在jvm中增加一个关闭的钩子，当jvm关闭的时候，会执行系统中已经设置的所有通过方法addShutdownHook添加的钩子，当系统执行完这些钩子后，jvm才会关闭。所以这些钩子可以在jvm关闭的时候进行内存清理、对象销毁等操作
        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.error("系统关闭")));
        try {
        	//加载spi服务类
            LoadSpiSupport(tccConfig);
            coordinatorService.start(tccConfig);
        } catch (Exception ex) {
            LogUtil.error(LOGGER, "tcc事务初始化异常:{}", ex::getMessage);
            System.exit(1);//非正常关闭
        }
        LogUtil.info(LOGGER, () -> "Tcc事务初始化成功！");
    }

    /**
     * 根据配置文件初始化spi
     *
     * @param tccConfig 配置信息
     */
    private void LoadSpiSupport(TccConfig tccConfig) {

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

        //将CoordinatorRepository实现注入到spring容器 ,同时将repositoryOptional的ObjectSerializer设置为serializer
        repositoryOptional.ifPresent(repository -> {
            serializer.ifPresent(repository::setSerializer);
            SpringBeanUtils.getInstance().registerBean(CoordinatorRepository.class.getName(), repository);
        });


    }
}
