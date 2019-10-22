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

package org.dromara.hmily.core.spi.repository;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.result.UpdateResult;
import org.dromara.hmily.annotation.HmilySPI;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.bean.adapter.MongoAdapter;
import org.dromara.hmily.common.bean.entity.HmilyParticipant;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.config.HmilyConfig;
import org.dromara.hmily.common.config.HmilyMongoConfig;
import org.dromara.hmily.common.enums.RepositorySupportEnum;
import org.dromara.hmily.common.exception.HmilyException;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.serializer.ObjectSerializer;
import org.dromara.hmily.common.utils.AssertUtils;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.utils.RepositoryPathUtils;
import org.dromara.hmily.core.spi.HmilyCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * mongo impl.
 *
 * @author xiaoyu
 */
@HmilySPI("mongodb")
public class MongoCoordinatorRepository implements HmilyCoordinatorRepository {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCoordinatorRepository.class);

    private ObjectSerializer objectSerializer;

    private MongoTemplate template;

    private String collectionName;

    @Override
    public int create(final HmilyTransaction hmilyTransaction) {
        try {
            MongoAdapter mongoBean = new MongoAdapter();
            mongoBean.setTransId(hmilyTransaction.getTransId());
            mongoBean.setCreateTime(hmilyTransaction.getCreateTime());
            mongoBean.setLastTime(hmilyTransaction.getLastTime());
            mongoBean.setRetriedCount(hmilyTransaction.getRetriedCount());
            mongoBean.setStatus(hmilyTransaction.getStatus());
            mongoBean.setRole(hmilyTransaction.getRole());
            mongoBean.setPattern(hmilyTransaction.getPattern());
            mongoBean.setTargetClass(hmilyTransaction.getTargetClass());
            mongoBean.setTargetMethod(hmilyTransaction.getTargetMethod());
            mongoBean.setConfirmMethod(hmilyTransaction.getConfirmMethod());
            mongoBean.setCancelMethod(hmilyTransaction.getCancelMethod());
            final byte[] cache = objectSerializer.serialize(hmilyTransaction.getHmilyParticipants());
            mongoBean.setContents(cache);
            template.save(mongoBean, collectionName);
        } catch (HmilyException e) {
            e.printStackTrace();
        }
        return ROWS;
    }

    @Override
    public int remove(final String id) {
        AssertUtils.notNull(id);
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(id));
        template.remove(query, collectionName);
        return ROWS;
    }

    @Override
    public int update(final HmilyTransaction hmilyTransaction) throws HmilyRuntimeException {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(hmilyTransaction.getTransId()));
        Update update = new Update();
        update.set("lastTime", new Date());
        update.set("retriedCount", hmilyTransaction.getRetriedCount());
        update.set("version", hmilyTransaction.getVersion() + 1);
        try {
            if (CollectionUtils.isNotEmpty(hmilyTransaction.getHmilyParticipants())) {
                update.set("contents", objectSerializer.serialize(hmilyTransaction.getHmilyParticipants()));
            }
        } catch (HmilyException e) {
            e.printStackTrace();
        }
        final UpdateResult updateResult = template.updateFirst(query, update, MongoAdapter.class, collectionName);

        if (updateResult.getModifiedCount() <= 0) {
            throw new HmilyRuntimeException("update data exception!");
        }
        return ROWS;
    }

    @Override
    public int updateParticipant(final HmilyTransaction hmilyTransaction) {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(hmilyTransaction.getTransId()));
        Update update = new Update();
        try {
            update.set("contents", objectSerializer.serialize(hmilyTransaction.getHmilyParticipants()));
        } catch (HmilyException e) {
            e.printStackTrace();
        }
        final UpdateResult updateResult = template.updateFirst(query, update, MongoAdapter.class, collectionName);
        if (updateResult.getModifiedCount() <= 0) {
            throw new HmilyRuntimeException("update data exception!");
        }
        return ROWS;
    }

    @Override
    public int updateStatus(final String id, final Integer status) {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(id));
        Update update = new Update();
        update.set("status", status);
        final UpdateResult updateResult = template.updateFirst(query, update, MongoAdapter.class, collectionName);
        if (updateResult.getModifiedCount() <= 0) {
            throw new HmilyRuntimeException("update data exception!");
        }
        return ROWS;
    }

    @Override
    public HmilyTransaction findById(final String id) {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(id));
        MongoAdapter cache = template.findOne(query, MongoAdapter.class, collectionName);
        return buildByCache(Objects.requireNonNull(cache));
    }

    @SuppressWarnings("unchecked")
    private HmilyTransaction buildByCache(final MongoAdapter cache) {
        try {
            HmilyTransaction hmilyTransaction = new HmilyTransaction();
            hmilyTransaction.setTransId(cache.getTransId());
            hmilyTransaction.setCreateTime(cache.getCreateTime());
            hmilyTransaction.setLastTime(cache.getLastTime());
            hmilyTransaction.setRetriedCount(cache.getRetriedCount());
            hmilyTransaction.setVersion(cache.getVersion());
            hmilyTransaction.setStatus(cache.getStatus());
            hmilyTransaction.setRole(cache.getRole());
            hmilyTransaction.setPattern(cache.getPattern());
            hmilyTransaction.setTargetClass(cache.getTargetClass());
            hmilyTransaction.setTargetMethod(cache.getTargetMethod());
            List<HmilyParticipant> hmilyParticipants = (List<HmilyParticipant>) objectSerializer.deSerialize(cache.getContents(), CopyOnWriteArrayList.class);
            hmilyTransaction.setHmilyParticipants(hmilyParticipants);
            return hmilyTransaction;
        } catch (HmilyException e) {
            LogUtil.error(LOGGER, "mongodb deSerialize exception:{}", e::getLocalizedMessage);
            return null;
        }

    }

    @Override
    public List<HmilyTransaction> listAll() {
        final List<MongoAdapter> resultList = template.findAll(MongoAdapter.class, collectionName);
        if (CollectionUtils.isNotEmpty(resultList)) {
            return resultList.stream().map(this::buildByCache).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<HmilyTransaction> listAllByDelay(final Date date) {
        Query query = new Query();
        query.addCriteria(Criteria.where("lastTime").lt(date));
        final List<MongoAdapter> mongoBeans =
                template.find(query, MongoAdapter.class, collectionName);
        if (CollectionUtils.isNotEmpty(mongoBeans)) {
            return mongoBeans.stream().map(this::buildByCache).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public void init(final String modelName, final HmilyConfig hmilyConfig) {
        collectionName = RepositoryPathUtils.buildMongoTableName(modelName);
        final HmilyMongoConfig hmilyMongoConfig = hmilyConfig.getHmilyMongoConfig();
        MongoClientFactoryBean clientFactoryBean = buildMongoClientFactoryBean(hmilyMongoConfig);
        try {
            clientFactoryBean.afterPropertiesSet();
            template = new MongoTemplate(Objects.requireNonNull(clientFactoryBean.getObject()), hmilyMongoConfig.getMongoDbName());
        } catch (Exception e) {
            LogUtil.error(LOGGER, "mongo init error please check you config:{}", e::getMessage);
            throw new HmilyRuntimeException(e);
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
    public String getScheme() {
        return RepositorySupportEnum.MONGODB.getSupport();
    }

    @Override
    public void setSerializer(final ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }
}
