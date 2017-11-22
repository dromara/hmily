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
package com.happylifeplat.tcc.core.spi.repository;

import com.google.common.base.Splitter;
import com.happylifeplat.tcc.common.bean.adapter.MongoAdapter;
import com.happylifeplat.tcc.common.bean.entity.Participant;
import com.happylifeplat.tcc.common.bean.entity.TccTransaction;
import com.happylifeplat.tcc.common.config.TccConfig;
import com.happylifeplat.tcc.common.config.TccMongoConfig;
import com.happylifeplat.tcc.common.enums.RepositorySupportEnum;
import com.happylifeplat.tcc.common.exception.TccException;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;
import com.happylifeplat.tcc.common.serializer.ObjectSerializer;
import com.happylifeplat.tcc.common.utils.AssertUtils;
import com.happylifeplat.tcc.common.utils.LogUtil;
import com.happylifeplat.tcc.common.utils.RepositoryPathUtils;
import com.happylifeplat.tcc.core.spi.CoordinatorRepository;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author xiaoyu
 */
public class MongoCoordinatorRepository implements CoordinatorRepository {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCoordinatorRepository.class);

    private ObjectSerializer objectSerializer;

    private MongoTemplate template;

    private String collectionName;


    /**
     * 创建本地事务对象
     *
     * @param tccTransaction 事务对象
     * @return rows
     */
    @Override
    public int create(TccTransaction tccTransaction) {
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
        return 1;
    }

    /**
     * 删除对象
     *
     * @param id 事务对象id
     * @return rows
     */
    @Override
    public int remove(String id) {
        AssertUtils.notNull(id);
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(id));
        template.remove(query, collectionName);
        return 1;
    }

    /**
     * 更新数据
     *
     * @param tccTransaction 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     */
    @Override
    public int update(TccTransaction tccTransaction) throws TccRuntimeException {
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

        final WriteResult writeResult = template.updateFirst(query, update, MongoAdapter.class, collectionName);
        if (writeResult.getN() <= 0) {
            //throw new TccRuntimeException("更新数据异常!");
        }
        return 1;
    }

    /**
     * 更新 List<Participant>  只更新这一个字段数据
     *
     * @param tccTransaction 实体对象
     */
    @Override
    public int updateParticipant(TccTransaction tccTransaction) {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(tccTransaction.getTransId()));
        Update update = new Update();
        try {
            update.set("contents", objectSerializer.serialize(tccTransaction.getParticipants()));
        } catch (TccException e) {
            e.printStackTrace();
        }
        final WriteResult writeResult = template.updateFirst(query, update, MongoAdapter.class, collectionName);
        if (writeResult.getN() <= 0) {
            throw new TccRuntimeException("更新数据异常!");
        }
        return 1;
    }

    /**
     * 更新补偿数据状态
     *
     * @param id     事务id
     * @param status 状态
     * @return rows 1 成功 0 失败
     */
    @Override
    public int updateStatus(String id, Integer status) {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(id));
        Update update = new Update();
        update.set("status", status);
        final WriteResult writeResult = template.updateFirst(query, update, MongoAdapter.class, collectionName);
        if (writeResult.getN() <= 0) {
            throw new TccRuntimeException("更新数据异常!");
        }
        return 1;
    }


    /**
     * 根据id获取对象
     *
     * @param id 主键id
     * @return TccTransaction
     */
    @Override
    public TccTransaction findById(String id) {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(id));
        MongoAdapter cache = template.findOne(query, MongoAdapter.class, collectionName);
        return buildByCache(cache);

    }

    @SuppressWarnings("unchecked")
    private TccTransaction buildByCache(MongoAdapter cache) {
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

    /**
     * 获取需要提交的事务
     *
     * @return List<TransactionRecover>
     */
    @Override
    public List<TccTransaction> listAll() {
        final List<MongoAdapter> resultList = template.findAll(MongoAdapter.class, collectionName);
        if (CollectionUtils.isNotEmpty(resultList)) {
            return resultList.stream().map(this::buildByCache).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<TccTransaction>
     */
    @Override
    public List<TccTransaction> listAllByDelay(Date date) {

        Query query = new Query();
        query.addCriteria(Criteria.where("lastTime").lt(date));
        final List<MongoAdapter> mongoBeans =
                template.find(query, MongoAdapter.class, collectionName);
        if (CollectionUtils.isNotEmpty(mongoBeans)) {
            return mongoBeans.stream().map(this::buildByCache).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 初始化操作
     *
     * @param modelName 模块名称
     * @param tccConfig 配置信息
     */
    @Override
    public void init(String modelName, TccConfig tccConfig) {
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

    /**
     * 生成mongoClientFacotryBean
     *
     * @param tccMongoConfig 配置信息
     * @return bean
     */
    private MongoClientFactoryBean buildMongoClientFactoryBean(TccMongoConfig tccMongoConfig) {
        MongoClientFactoryBean clientFactoryBean = new MongoClientFactoryBean();
        MongoCredential credential = MongoCredential.createScramSha1Credential(tccMongoConfig.getMongoUserName(),
                tccMongoConfig.getMongoDbName(),
                tccMongoConfig.getMongoUserPwd().toCharArray());
        clientFactoryBean.setCredentials(new MongoCredential[]{
                credential
        });
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

    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return RepositorySupportEnum.MONGODB.getSupport();
    }

    /**
     * 设置序列化信息
     *
     * @param objectSerializer 序列化实现
     */
    @Override
    public void setSerializer(ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }
}
