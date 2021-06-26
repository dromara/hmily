/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dromara.hmily.repository.mongodb;

import org.dromara.hmily.repository.mongodb.entity.XaRecoveryMongoEntity;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.HmilyXaRepository;
import org.dromara.hmily.repository.spi.entity.HmilyXaRecovery;
import org.dromara.hmily.repository.spi.entity.HmilyXaRecoveryImpl;
import org.dromara.hmily.spi.HmilySPI;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MongodbXaRepository .
 * 增加Xa相关的事务查询，通过order让系统加载的时候提高优先级。
 *
 * @author sixh chenbin
 * @see MongodbRepository
 */
@HmilySPI(value = "mongodb", order = 100)
public class MongodbXaRepository extends MongodbRepository implements HmilyXaRepository, HmilyRepository {

    /**
     * The Service.
     */
    private MongodbTemplateService service;

    @Override
    public void init(final String appName) {
        super.init(appName);
        service = super.getService();
    }

    @Override
    public List<HmilyXaRecovery> queryByTmUnique(final String tmUnique, final Integer state) {
        return service.find(XaRecoveryMongoEntity.class, Criteria.where("tm_unique").is(tmUnique).and("state").is(state))
                .stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public void addLog(final HmilyXaRecovery t) {
        XaRecoveryMongoEntity impl = new XaRecoveryMongoEntity();
        impl.setCreateTime(t.getCreateTime());
        impl.setUpdateTime(t.getUpdateTime());
        impl.setBranchId(t.getBranchId());
        impl.setEndBxid(t.getEndBxid());
        impl.setEndXid(t.getEndXid());
        impl.setGlobalId(t.getGlobalId());
        impl.setIsCoordinator(t.getIsCoordinator());
        impl.setState(t.getState());
        impl.setSuperId(t.getSuperId());
        impl.setTmUnique(t.getTmUnique());
        impl.setUrl(t.getUrl());
        impl.setVersion(t.getVersion());
        int insertc = service.insertc(impl);
        if (insertc <= 0) {
            throw new RuntimeException("addLog error");
        }
    }

    /**
     * Convert hmily xa recovery.
     *
     * @param entity the entity
     * @return the hmily xa recovery
     */
    private HmilyXaRecovery convert(final XaRecoveryMongoEntity entity) {
        return HmilyXaRecoveryImpl.convert(entity);
    }
}
