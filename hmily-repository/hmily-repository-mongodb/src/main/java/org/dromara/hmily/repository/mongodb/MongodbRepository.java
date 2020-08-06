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
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.dromara.hmily.config.HmilyConfig;
import org.dromara.hmily.config.HmilyMongoConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.spi.HmilySPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * mongo impl.
 *
 * @author xiaoyu
 */
@HmilySPI("mongodb")
public class MongodbRepository implements HmilyRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbRepository.class);
    
    private HmilySerializer hmilySerializer;
    
    private MongoTemplate template;
    
    private String collectionName;
    
    @Override
    public void init(final HmilyConfig hmilyConfig) {
        collectionName = hmilyConfig.getAppName();
        final HmilyMongoConfig hmilyMongoConfig = hmilyConfig.getHmilyMongoConfig();
        MongoClientFactoryBean clientFactoryBean = buildMongoClientFactoryBean(hmilyMongoConfig);
        try {
            clientFactoryBean.afterPropertiesSet();
            template = new MongoTemplate(Objects.requireNonNull(clientFactoryBean.getObject()), hmilyMongoConfig.getMongoDbName());
        } catch (Exception e) {
            LOGGER.error("mongo init error please check you config:{}", e.getMessage());
            throw new HmilyRepositoryException(e);
        }
    }
    
    private MongoClientFactoryBean buildMongoClientFactoryBean(final HmilyMongoConfig hmilyMongoConfig) {
        MongoClientFactoryBean clientFactoryBean = new MongoClientFactoryBean();
        MongoCredential credential = MongoCredential.createScramSha1Credential(hmilyMongoConfig.getMongoUserName(),
                hmilyMongoConfig.getMongoDbName(),
                hmilyMongoConfig.getMongoUserPwd().toCharArray());
        clientFactoryBean.setCredentials(new MongoCredential[]{credential});
        List<String> urls = Lists.newArrayList(Splitter.on(",").trimResults().split(hmilyMongoConfig.getMongoDbUrl()));
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
        this.hmilySerializer = hmilySerializer;
    }
    
    @Override
    public int createHmilyTransaction(HmilyTransaction hmilyTransaction) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public int updateRetryByLock(HmilyTransaction hmilyTransaction) {
        return 0;
    }
    
    @Override
    public HmilyTransaction findByTransId(Long transId) {
        return null;
    }
    
    @Override
    public List<HmilyTransaction> listLimitByDelay(Date date, int limit) {
        return null;
    }
    
    @Override
    public int updateHmilyTransactionStatus(Long transId, Integer status) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public int removeHmilyTransaction(Long transId) {
        return 0;
    }
    
    @Override
    public int removeHmilyTransactionByData(Date date) {
        return 0;
    }
    
    @Override
    public int createHmilyParticipant(HmilyParticipant hmilyParticipant) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public List<HmilyParticipant> findHmilyParticipant(Long participantId) {
        return null;
    }
    
    @Override
    public List<HmilyParticipant> listHmilyParticipant(Date date, String transType, int limit) {
        return null;
    }
    
    @Override
    public List<HmilyParticipant> listHmilyParticipantByTransId(Long transId) {
        return null;
    }
    
    @Override
    public boolean existHmilyParticipantByTransId(Long transId) {
        return false;
    }
    
    @Override
    public int updateHmilyParticipantStatus(Long participantId, Integer status) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public int removeHmilyParticipant(Long participantId) {
        return 0;
    }
    
    @Override
    public int removeHmilyParticipantByData(Date date) {
        return 0;
    }
    
    @Override
    public boolean lockHmilyParticipant(HmilyParticipant hmilyParticipant) {
        return false;
    }
    
    @Override
    public int createHmilyParticipantUndo(HmilyParticipantUndo hmilyParticipantUndo) {
        return 0;
    }
    
    @Override
    public List<HmilyParticipantUndo> findHmilyParticipantUndoByParticipantId(Long participantId) {
        return null;
    }
    
    @Override
    public int removeHmilyParticipantUndo(Long undoId) {
        return 0;
    }
    
    @Override
    public int removeHmilyParticipantUndoByData(Date date) {
        return 0;
    }
    
    @Override
    public int updateHmilyParticipantUndoStatus(Long undoId, Integer status) {
        return 0;
    }
}
