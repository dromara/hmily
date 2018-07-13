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

package com.hmily.tcc.core.spi.repository;

import com.google.common.base.Splitter;
import com.hmily.tcc.common.bean.adapter.MongoAdapter;
import com.hmily.tcc.common.bean.entity.Participant;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.config.TccConfig;
import com.hmily.tcc.common.config.TccMongoConfig;
import com.hmily.tcc.common.enums.RepositorySupportEnum;
import com.hmily.tcc.common.exception.TccException;
import com.hmily.tcc.common.exception.TccRuntimeException;
import com.hmily.tcc.common.serializer.ObjectSerializer;
import com.hmily.tcc.common.utils.AssertUtils;
import com.hmily.tcc.common.utils.LogUtil;
import com.hmily.tcc.common.utils.RepositoryPathUtils;
import com.hmily.tcc.core.spi.CoordinatorRepository;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.collections.CollectionUtils;
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
 * @author xiaoyu
 */
public class MongoCoordinatorRepository implements CoordinatorRepository {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCoordinatorRepository.class);

    private ObjectSerializer objectSerializer;

    private MongoTemplate template;

    private String collectionName;

    @Override
    public int create(final TccTransaction tccTransaction) {
        try {
            MongoAdapter mongoBean = new MongoAdapter();
            mongoBean.setTransId(tccTransaction.getTransId());
            mongoBean.setCreateTime(tccTransaction.getCreateTime());
            mongoBean.setLastTime(tccTransaction.getLastTime());
            mongoBean.setRetriedCount(tccTransaction.getRetriedCount());
            mongoBean.setStatus(tccTransaction.getStatus());
            mongoBean.setRole(tccTransaction.getRole());
            mongoBean.setPattern(tccTransaction.getPattern());
            mongoBean.setTargetClass(tccTransaction.getTargetClass());
            mongoBean.setTargetMethod(tccTransaction.getTargetMethod());
            mongoBean.setConfirmMethod("");
            mongoBean.setCancelMethod("");
            final byte[] cache = objectSerializer.serialize(tccTransaction.getParticipants());
            mongoBean.setContents(cache);
            template.save(mongoBean, collectionName);
        } catch (TccException e) {
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
    public int update(final TccTransaction tccTransaction) throws TccRuntimeException {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(tccTransaction.getTransId()));
        Update update = new Update();
        update.set("lastTime", new Date());
        update.set("retriedCount", tccTransaction.getRetriedCount() + 1);
        update.set("version", tccTransaction.getVersion() + 1);
        try {
            if (CollectionUtils.isNotEmpty(tccTransaction.getParticipants())) {
                final Participant participant = tccTransaction.getParticipants().get(0);
                if (Objects.nonNull(participant)) {
                    update.set("confirmMethod", participant.getConfirmTccInvocation().getMethodName());
                    update.set("cancelMethod", participant.getCancelTccInvocation().getMethodName());
                }
                update.set("contents", objectSerializer.serialize(tccTransaction.getParticipants()));
            }
        } catch (TccException e) {
            e.printStackTrace();
        }
        final UpdateResult updateResult = template.updateFirst(query, update, MongoAdapter.class, collectionName);

        if (updateResult.getModifiedCount() <= 0) {
            throw new TccRuntimeException("更新数据异常!");
        }
        return ROWS;
    }

    @Override
    public int updateParticipant(final TccTransaction tccTransaction) {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(tccTransaction.getTransId()));
        Update update = new Update();
        try {
            update.set("contents", objectSerializer.serialize(tccTransaction.getParticipants()));
        } catch (TccException e) {
            e.printStackTrace();
        }
        final UpdateResult updateResult = template.updateFirst(query, update, MongoAdapter.class, collectionName);
        if (updateResult.getModifiedCount() <= 0) {
            throw new TccRuntimeException("更新数据异常!");
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
            throw new TccRuntimeException("更新数据异常!");
        }
        return ROWS;
    }

    @Override
    public TccTransaction findById(final String id) {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(id));
        MongoAdapter cache = template.findOne(query, MongoAdapter.class, collectionName);
        return buildByCache(cache);
    }

    @SuppressWarnings("unchecked")
    private TccTransaction buildByCache(final MongoAdapter cache) {
        TccTransaction tccTransaction = new TccTransaction();
        tccTransaction.setTransId(cache.getTransId());
        tccTransaction.setCreateTime(cache.getCreateTime());
        tccTransaction.setLastTime(cache.getLastTime());
        tccTransaction.setRetriedCount(cache.getRetriedCount());
        tccTransaction.setVersion(cache.getVersion());
        tccTransaction.setStatus(cache.getStatus());
        tccTransaction.setRole(cache.getRole());
        tccTransaction.setPattern(cache.getPattern());
        tccTransaction.setTargetClass(cache.getTargetClass());
        tccTransaction.setTargetMethod(cache.getTargetMethod());
        try {
            List<Participant> participants = (List<Participant>) objectSerializer.deSerialize(cache.getContents(), CopyOnWriteArrayList.class);
            tccTransaction.setParticipants(participants);
        } catch (TccException e) {
            LogUtil.error(LOGGER, "mongodb 反序列化异常:{}", e::getLocalizedMessage);
        }
        return tccTransaction;
    }

    @Override
    public List<TccTransaction> listAll() {
        final List<MongoAdapter> resultList = template.findAll(MongoAdapter.class, collectionName);
        if (CollectionUtils.isNotEmpty(resultList)) {
            return resultList.stream().map(this::buildByCache).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<TccTransaction> listAllByDelay(final Date date) {
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
    public void init(final String modelName, final TccConfig tccConfig) {
        collectionName = RepositoryPathUtils.buildMongoTableName(modelName);
        final TccMongoConfig tccMongoConfig = tccConfig.getTccMongoConfig();
        MongoClientFactoryBean clientFactoryBean = buildMongoClientFactoryBean(tccMongoConfig);
        try {
            clientFactoryBean.afterPropertiesSet();
            template = new MongoTemplate(clientFactoryBean.getObject(), tccMongoConfig.getMongoDbName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MongoClientFactoryBean buildMongoClientFactoryBean(final TccMongoConfig tccMongoConfig) {
        MongoClientFactoryBean clientFactoryBean = new MongoClientFactoryBean();
        MongoCredential credential = MongoCredential.createScramSha1Credential(tccMongoConfig.getMongoUserName(),
                tccMongoConfig.getMongoDbName(),
                tccMongoConfig.getMongoUserPwd().toCharArray());
        clientFactoryBean.setCredentials(new MongoCredential[]{credential});

        List<String> urls = Splitter.on(",").trimResults().splitToList(tccMongoConfig.getMongoDbUrl());
        ServerAddress[] sds = new ServerAddress[urls.size()];
        for (int i = 0; i < sds.length; i++) {
            List<String> adds = Splitter.on(":").trimResults().splitToList(urls.get(i));
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
