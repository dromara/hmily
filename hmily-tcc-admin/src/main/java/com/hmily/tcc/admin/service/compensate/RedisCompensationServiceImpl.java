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
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * redis impl.
 * @author xiaoyu(Myth)
 */
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class RedisCompensationServiceImpl implements CompensationService {

    private final JedisClient jedisClient;

    private final ObjectSerializer objectSerializer;

    @Override
    public CommonPager<TccCompensationVO> listByPage(final CompensationQuery query) {
        CommonPager<TccCompensationVO> commonPager = new CommonPager<>();
        final String redisKeyPrefix = RepositoryPathUtils.buildRedisKeyPrefix(query.getApplicationName());
        final int currentPage = query.getPageParameter().getCurrentPage();
        final int pageSize = query.getPageParameter().getPageSize();
        int start = (currentPage - 1) * pageSize;
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

    private TccCompensationVO buildVOByKey(final byte[] key) {
        final byte[] bytes = jedisClient.get(key);
        try {
            final CoordinatorRepositoryAdapter adapter =
                    objectSerializer.deSerialize(bytes, CoordinatorRepositoryAdapter.class);
            return ConvertHelper.buildVO(adapter);
        } catch (TccException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Boolean batchRemove(final List<String> ids, final String applicationName) {
        if (CollectionUtils.isEmpty(ids) || StringUtils.isBlank(applicationName)) {
            return Boolean.FALSE;
        }
        String keyPrefix = RepositoryPathUtils.buildRedisKeyPrefix(applicationName);
        final String[] keys = ids.stream()
                .map(id -> RepositoryPathUtils.buildRedisKey(keyPrefix, id)).toArray(String[]::new);
        jedisClient.del(keys);
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateRetry(final String id, final Integer retry, final String appName) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(appName) || Objects.isNull(retry)) {
            return Boolean.FALSE;
        }
        String keyPrefix = RepositoryPathUtils.buildRedisKeyPrefix(appName);
        final String key = RepositoryPathUtils.buildRedisKey(keyPrefix, id);
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

    private List<TccCompensationVO> findAll(final Set<byte[]> keys) {
        return keys.parallelStream()
                .map(this::buildVOByKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<TccCompensationVO> findByPage(final Set<byte[]> keys, final int start, final int pageSize) {
        return keys.parallelStream()
                .skip(start)
                .limit(pageSize)
                .map(this::buildVOByKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
