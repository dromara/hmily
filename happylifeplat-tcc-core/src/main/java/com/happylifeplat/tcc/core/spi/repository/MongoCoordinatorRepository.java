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
import com.happylifeplat.tcc.common.config.TccConfig;
import com.happylifeplat.tcc.common.config.TccMongoConfig;
import com.happylifeplat.tcc.core.bean.entity.Participant;
import com.happylifeplat.tcc.core.bean.entity.TccTransaction;
import com.happylifeplat.tcc.common.enums.RepositorySupportEnum;
import com.happylifeplat.tcc.common.enums.TccActionEnum;
import com.happylifeplat.tcc.common.exception.TccException;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;
import com.happylifeplat.tcc.common.utils.AssertUtils;
import com.happylifeplat.tcc.common.utils.LogUtil;
import com.happylifeplat.tcc.core.bean.MongoBean;
import com.happylifeplat.tcc.core.spi.ObjectSerializer;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MongoCoordinatorRepository implements CoordinatorRepository {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCoordinatorRepository.class);

    private ObjectSerializer objectSerializer;

    private MongoTemplate template;

    private String COLLECTION_NAME;


    /**
     * 创建本地事务对象
     *
     * @param tccTransaction 事务对象
     * @return rows
     */
    @Override
    public int create(TccTransaction tccTransaction) {
        try {
            MongoBean mongoBean = new MongoBean();
            mongoBean.setTransId(tccTransaction.getTransId());
            mongoBean.setCreateTime(tccTransaction.getCreateTime());
            mongoBean.setLastTime(tccTransaction.getLastTime());
            mongoBean.setRetriedCount(tccTransaction.getRetriedCount());
            mongoBean.setStatus(tccTransaction.getStatus());
            mongoBean.setRole(tccTransaction.getRole());
            mongoBean.setPattern(tccTransaction.getPattern());
            final byte[] cache = objectSerializer.serialize(tccTransaction.getParticipants());
            mongoBean.setContents(cache);
            template.save(mongoBean, COLLECTION_NAME);
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
        template.remove(query, COLLECTION_NAME);
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
        final WriteResult writeResult = template.updateFirst(query, update, MongoBean.class, COLLECTION_NAME);
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
        MongoBean cache = template.findOne(query, MongoBean.class, COLLECTION_NAME);
        return buildByCache(cache);

    }

    @SuppressWarnings("unchecked")
    private TccTransaction buildByCache(MongoBean cache) {
        TccTransaction tccTransaction = new TccTransaction();
        tccTransaction.setTransId(cache.getTransId());
        tccTransaction.setCreateTime(cache.getCreateTime());
        tccTransaction.setLastTime(cache.getLastTime());
        tccTransaction.setRetriedCount(cache.getRetriedCount());
        tccTransaction.setVersion(cache.getVersion());
        tccTransaction.setStatus(cache.getStatus());
        tccTransaction.setRole(cache.getRole());
        tccTransaction.setPattern(cache.getPattern());
        try {
            List<Participant> participants = objectSerializer.deSerialize(cache.getContents(), List.class);
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
        final List<MongoBean> resultList = template.findAll(MongoBean.class, COLLECTION_NAME);
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
        final List<MongoBean> mongoBeans =
                template.find(query, MongoBean.class, COLLECTION_NAME);
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
        COLLECTION_NAME = modelName;
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
