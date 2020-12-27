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

package org.dromara.hmily.repository.mongodb;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apache.commons.lang3.tuple.Pair;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyMongoConfig;
import org.dromara.hmily.repository.mongodb.entity.ParticipantMongoEntity;
import org.dromara.hmily.repository.mongodb.entity.TransactionMongoEntity;
import org.dromara.hmily.repository.mongodb.entity.UndoMongoEntity;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyLock;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.spi.HmilySPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.query.Criteria;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * mongo impl.
 *
 * @author xiaoyu
 */
@HmilySPI("mongodb")
public class MongodbRepository implements HmilyRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbRepository.class);
    
    private MongoEntityConvert converter;
    
    private MongodbTemplateService service;

    private String appName;

    @Override
    public void init(final String appName) {
        this.appName = appName;
        HmilyMongoConfig hmilyMongoConfig = ConfigEnv.getInstance().getConfig(HmilyMongoConfig.class);
        MongoClientFactoryBean clientFactoryBean = buildMongoClientFactoryBean(hmilyMongoConfig);
        try {
            clientFactoryBean.afterPropertiesSet();
            service = new MongodbTemplateService(Objects.requireNonNull(clientFactoryBean.getObject()), hmilyMongoConfig.getDatabaseName());
            
        } catch (Exception e) {
            LOGGER.error("mongo init error please check you config:{}", e.getMessage());
            throw new HmilyRepositoryException(e);
        }
    }
    
    private MongoClientFactoryBean buildMongoClientFactoryBean(final HmilyMongoConfig hmilyMongoConfig) {
        MongoClientFactoryBean clientFactoryBean = new MongoClientFactoryBean();
        MongoCredential credential = MongoCredential.createScramSha1Credential(hmilyMongoConfig.getUserName(),
                hmilyMongoConfig.getDatabaseName(),
                hmilyMongoConfig.getPassword().toCharArray());
        clientFactoryBean.setCredentials(new MongoCredential[]{credential});
        List<String> urls = Lists.newArrayList(Splitter.on(",").trimResults().split(hmilyMongoConfig.getUrl()));
        ServerAddress[] sds = new ServerAddress[urls.size()];
        for (int i = 0; i < sds.length; i++) {
            List<String> adds = Lists.newArrayList(Splitter.on(":").trimResults().split(urls.get(i)));
            InetSocketAddress address = new InetSocketAddress(adds.get(0), Integer.parseInt(adds.get(1)));
            sds[i] = new ServerAddress(address);
        }
        clientFactoryBean.setReplicaSetSeeds(sds);
        return clientFactoryBean;
    }
    
    @Override
    public void setSerializer(final HmilySerializer hmilySerializer) {
        this.converter = new MongoEntityConvert(hmilySerializer);
    }
    
    @Override
    public int createHmilyTransaction(final HmilyTransaction hmilyTransaction) {
        return service.insertc(converter.create(hmilyTransaction, appName));
    }

    @Override
    public int updateRetryByLock(final HmilyTransaction hmilyTransaction) {
        return service.update(TransactionMongoEntity.class,
                Criteria.where("trans_id").is(hmilyTransaction.getTransId())
                .and("version").is(hmilyTransaction.getVersion()),
                set("version", hmilyTransaction.getVersion() + 1),
                set("retry", hmilyTransaction.getRetry() + 1)
                );
    }
    
    @Override
    public HmilyTransaction findByTransId(final Long transId) {
        return service.find(TransactionMongoEntity.class, Criteria.where("trans_id").is(transId))
                .stream().map(converter::convert).findFirst().orElse(null);
    }
    
    @Override
    public List<HmilyTransaction> listLimitByDelay(final Date date, final int limit) {
        return service.find(TransactionMongoEntity.class,
                Criteria.where("update_time").lt(date)
                .and("app_name").is(appName), limit)
                .stream().filter(Objects::nonNull).map(converter::convert)
                .collect(Collectors.toList());
    }
    
    @Override
    public int updateHmilyTransactionStatus(final Long transId, final Integer status) throws HmilyRepositoryException {
        return service.update(TransactionMongoEntity.class,
                Criteria.where("trans_id").is(transId),
                set("status", status));
    }

    @Override
    public int removeHmilyTransaction(final Long transId) {
        return service.delete(TransactionMongoEntity.class, Criteria.where("trans_id").is(transId));
    }

    @Override
    public int createHmilyParticipant(final HmilyParticipant hmilyParticipant) throws HmilyRepositoryException {
        return service.insertc(converter.create(hmilyParticipant, appName));
    }

    @Override
    public List<HmilyParticipant> findHmilyParticipant(final Long participantId) {
        List<HmilyParticipant> hmilyParticipantList = new ArrayList<>();
        HmilyParticipant hmilyParticipant = service.find(ParticipantMongoEntity.class,
                Criteria.where("participant_id").is(participantId))
                .stream().filter(Objects::nonNull)
                .map(converter::convert)
                .findFirst().orElse(null);
        if (hmilyParticipant != null) {

            hmilyParticipantList.add(hmilyParticipant);
            service.find(ParticipantMongoEntity.class,
                    Criteria.where("participant_ref_id").is(participantId))
                    .stream().filter(Objects::nonNull)
                    .map(converter::convert)
                    .collect(Collectors.toCollection(() -> hmilyParticipantList));
        }
        return hmilyParticipantList;
    }

    @Override
    public List<HmilyParticipant> listHmilyParticipant(final Date date, final String transType, final int limit) {
        return service.find(ParticipantMongoEntity.class,
                Criteria.where("update_time").lt(date)
                    .and("app_name").is(appName)
                    .and("trans_type").is(transType)
                    .and("status").nin(4, 8), limit)
                .stream().filter(Objects::nonNull).map(converter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<HmilyParticipant> listHmilyParticipantByTransId(final Long transId) {
        return service.find(ParticipantMongoEntity.class,
                Criteria.where("trans_id").is(transId))
                .stream().filter(Objects::nonNull).map(converter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existHmilyParticipantByTransId(final Long transId) {
        return service.count(ParticipantMongoEntity.class, Criteria.where("trans_id").is(transId)) > 0;
    }

    @Override
    public int createHmilyParticipantUndo(final HmilyParticipantUndo undo) {
        return service.insertc(converter.create(undo));
    }

    @Override
    public List<HmilyParticipantUndo> findHmilyParticipantUndoByParticipantId(final Long participantId) {
        return service.find(UndoMongoEntity.class, Criteria.where("participant_id").is(participantId))
                .stream().map(converter::convert).collect(Collectors.toList());
    }

    @Override
    public int updateHmilyParticipantUndoStatus(final Long undoId, final Integer status) {
        return service.update(UndoMongoEntity.class, Criteria.where("undo_id").is(undoId), set("status", status));
    }
    
    @Override
    public int writeHmilyLocks(final Collection<HmilyLock> locks) {
        // TODO
        return 0;
    }
    
    @Override
    public int releaseHmilyLocks(final Collection<HmilyLock> locks) {
        // TODO
        return 0;
    }
    
    @Override
    public Optional<HmilyLock> findHmilyLockById(final String lockId) {
        // TODO
        return Optional.empty();
    }
    
    @Override
    public int removeHmilyTransactionByDate(final Date date) {
        return service.delete(TransactionMongoEntity.class,
                Criteria.where("update_time").lt(date).and("status").is(4));
    }

    @Override
    public int removeHmilyParticipantByDate(final Date date) {
        return service.delete(ParticipantMongoEntity.class,
                Criteria.where("update_time").lt(date).and("status").is(4));
    }

    @Override
    public boolean lockHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        return service.update(ParticipantMongoEntity.class,
                Criteria.where("participant_id").is(hmilyParticipant.getParticipantId())
                .and("version").is(hmilyParticipant.getVersion()),
                set("version", hmilyParticipant.getVersion() + 1),
                set("retry", hmilyParticipant.getRetry() + 1)) > 0;
    }

    @Override
    public int removeHmilyParticipantUndoByDate(final Date date) {
        return service.delete(UndoMongoEntity.class,
                Criteria.where("update_time").lt(date).and("status").is(4));
    }

    @Override
    public int removeHmilyParticipantUndo(final Long undoId) {
        return service.delete(UndoMongoEntity.class,
                Criteria.where("undo_id").is(undoId));
    }

    @Override
    public int updateHmilyParticipantStatus(final Long participantId, final Integer status) {
        return service.update(ParticipantMongoEntity.class,
                Criteria.where("participant_id").is(participantId),
                set("status", status));
    }

    @Override
    public int removeHmilyParticipant(final Long participantId) {
        return service.delete(ParticipantMongoEntity.class,
                Criteria.where("participant_id").is(participantId));
    }

    private Pair<String, Object> set(final String key, final Object value) {
        return Pair.of(key, value);
    }

}
