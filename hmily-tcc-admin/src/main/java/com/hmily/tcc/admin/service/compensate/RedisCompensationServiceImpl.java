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

package com.hmily.tcc.admin.service.compensate;

import com.google.common.collect.Sets;
import com.hmily.tcc.admin.helper.ConvertHelper;
import com.hmily.tcc.admin.helper.PageHelper;
import com.hmily.tcc.admin.page.CommonPager;
import com.hmily.tcc.admin.query.CompensationQuery;
import com.hmily.tcc.admin.service.CompensationService;
import com.hmily.tcc.admin.vo.TccCompensationVO;
import com.hmily.tcc.common.bean.adapter.CoordinatorRepositoryAdapter;
import com.hmily.tcc.common.exception.TccException;
import com.hmily.tcc.common.jedis.JedisClient;
import com.hmily.tcc.common.serializer.ObjectSerializer;
import com.hmily.tcc.common.utils.DateUtils;
import com.hmily.tcc.common.utils.RepositoryPathUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Description: .</p>
 * redis实现
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/19 17:08
 * @since JDK 1.8
 */
@SuppressWarnings("unchecked")
public class RedisCompensationServiceImpl implements CompensationService {


    private JedisClient jedisClient;


    public RedisCompensationServiceImpl(JedisClient jedisClient) {
        this.jedisClient = jedisClient;
    }


    @Autowired
    private ObjectSerializer objectSerializer;


    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCompensationServiceImpl.class);

    /**
     * 分页获取补偿事务信息
     *
     * @param query 查询条件
     * @return CommonPager<TransactionRecoverVO>
     */
    @Override
    public CommonPager<TccCompensationVO> listByPage(CompensationQuery query) {

        CommonPager<TccCompensationVO> commonPager = new CommonPager<>();

        final String redisKeyPrefix = RepositoryPathUtils.buildRedisKeyPrefix(query.getApplicationName());

        final int currentPage = query.getPageParameter().getCurrentPage();
        final int pageSize = query.getPageParameter().getPageSize();

        int start = (currentPage - 1) * pageSize;


        //transaction:compensate:alipay-service:
        //获取所有的key
        Set<byte[]> keys;

        List<TccCompensationVO> voList;

        int totalCount;

        //如果只查 重试条件的
        if (StringUtils.isBlank(query.getTransId()) && Objects.nonNull(query.getRetry())) {
            keys = jedisClient.keys((redisKeyPrefix + "*").getBytes());
            final List<TccCompensationVO> all = findAll(keys);
            final List<TccCompensationVO> collect =
                    all.stream()
                            .filter(vo -> vo.getRetriedCount() < query.getRetry())
                            .collect(Collectors.toList());
            totalCount = collect.size();
            voList = collect.stream().skip(start).limit(pageSize).collect(Collectors.toList());
        } else if (StringUtils.isNoneBlank(query.getTransId()) && Objects.isNull(query.getRetry())) {
            keys = Sets.newHashSet(String.join(":", redisKeyPrefix, query.getTransId()).getBytes());
            totalCount = keys.size();
            voList = findAll(keys);
        } else if (StringUtils.isNoneBlank(query.getTransId()) && Objects.nonNull(query.getRetry())) {
            keys = Sets.newHashSet(String.join(":", redisKeyPrefix, query.getTransId()).getBytes());
            totalCount = keys.size();
            voList = findAll(keys)
                    .stream()
                    .filter(vo -> vo.getRetriedCount() < query.getRetry())
                    .collect(Collectors.toList());
        } else {
            keys = jedisClient.keys((redisKeyPrefix + "*").getBytes());
            if (keys.size() <= 0 || keys.size() < start) {
                return commonPager;
            }
            totalCount = keys.size();
            voList = findByPage(keys, start, pageSize);
        }

        if (keys.size() <= 0 || keys.size() < start) {
            return commonPager;
        }
        commonPager.setPage(PageHelper.buildPage(query.getPageParameter(), totalCount));
        commonPager.setDataList(voList);
        return commonPager;
    }





    private TccCompensationVO buildVOByKey(byte[] key) {
        final byte[] bytes = jedisClient.get(key);
        try {
            final CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(bytes, CoordinatorRepositoryAdapter.class);
            return ConvertHelper.buildVO(adapter);
        } catch (TccException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 批量删除补偿事务信息
     *
     * @param ids             ids 事务id集合
     * @param applicationName 应用名称
     * @return true 成功
     */
    @Override
    public Boolean batchRemove(List<String> ids, String applicationName) {
        if (CollectionUtils.isEmpty(ids) || StringUtils.isBlank(applicationName)) {
            return Boolean.FALSE;
        }
        String keyPrefix = RepositoryPathUtils.buildRedisKeyPrefix(applicationName);
        final String[] keys = ids.stream()
                .map(id -> RepositoryPathUtils.buildRedisKey(keyPrefix,id)).toArray(String[]::new);

        jedisClient.del(keys);
        return Boolean.TRUE;
    }

    /**
     * 更改恢复次数
     *
     * @param id              事务id
     * @param retry           恢复次数
     * @param applicationName 应用名称
     * @return true 成功
     */
    @Override
    public Boolean updateRetry(String id, Integer retry, String applicationName) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(applicationName) || Objects.isNull(retry)) {
            return Boolean.FALSE;
        }
        String keyPrefix = RepositoryPathUtils.buildRedisKeyPrefix(applicationName);
        final String key = RepositoryPathUtils.buildRedisKey(keyPrefix,id);
        final byte[] bytes = jedisClient.get(key.getBytes());
        try {
            final CoordinatorRepositoryAdapter adapter =
                    objectSerializer.deSerialize(bytes, CoordinatorRepositoryAdapter.class);
            adapter.setRetriedCount(retry);
            adapter.setLastTime(DateUtils.getDateYYYY());
            jedisClient.set(key, objectSerializer.serialize(adapter));
            return Boolean.TRUE;
        } catch (Exception e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }

    }


    private List<TccCompensationVO> findAll(Set<byte[]> keys) {
        return keys.parallelStream()
                .map(this::buildVOByKey).filter(Objects::nonNull).collect(Collectors.toList());
    }


    private List<TccCompensationVO> findByPage(Set<byte[]> keys, int start, int pageSize) {
        return keys.parallelStream().skip(start).limit(pageSize)
                .map(this::buildVOByKey).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
