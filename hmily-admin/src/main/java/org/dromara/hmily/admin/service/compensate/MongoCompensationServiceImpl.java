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

package org.dromara.hmily.admin.service.compensate;

import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.admin.helper.ConvertHelper;
import org.dromara.hmily.admin.helper.PageHelper;
import org.dromara.hmily.admin.page.CommonPager;
import org.dromara.hmily.admin.page.PageParameter;
import org.dromara.hmily.admin.query.CompensationQuery;
import org.dromara.hmily.admin.service.CompensationService;
import org.dromara.hmily.admin.vo.HmilyCompensationVO;
import org.dromara.hmily.common.bean.adapter.MongoAdapter;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.utils.DateUtils;
import org.dromara.hmily.common.utils.RepositoryPathUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Mongodb impl.
 * @author xiaoyu(Myth)
 */
@RequiredArgsConstructor
public class MongoCompensationServiceImpl implements CompensationService {

    private final MongoTemplate mongoTemplate;

    @Override
    public CommonPager<HmilyCompensationVO> listByPage(final CompensationQuery query) {
        CommonPager<HmilyCompensationVO> voCommonPager = new CommonPager<>();
        final String mongoTableName = RepositoryPathUtils.buildMongoTableName(query.getApplicationName());
        final PageParameter pageParameter = query.getPageParameter();
        final int pageSize = pageParameter.getPageSize();
        Query baseQuery = new Query();
        if (StringUtils.isNoneBlank(query.getTransId())) {
            baseQuery.addCriteria(new Criteria("transId").is(query.getTransId()));
        }
        if (Objects.nonNull(query.getRetry())) {
            baseQuery.addCriteria(new Criteria("retriedCount").lt(query.getRetry()));
        }
        final long totalCount = mongoTemplate.count(baseQuery, mongoTableName);
        if (totalCount <= 0) {
            return voCommonPager;
        }
        final int currentPage = pageParameter.getCurrentPage();
        int start = (currentPage - 1) * pageSize;
        voCommonPager.setPage(PageHelper.buildPage(query.getPageParameter(), (int) totalCount));
        baseQuery.skip(start).limit(pageSize);
        final List<MongoAdapter> mongoAdapters =
                mongoTemplate.find(baseQuery, MongoAdapter.class, mongoTableName);
        if (CollectionUtils.isNotEmpty(mongoAdapters)) {
            final List<HmilyCompensationVO> recoverVOS =
                    mongoAdapters
                            .stream()
                            .map(ConvertHelper::buildVO)
                            .collect(Collectors.toList());
            voCommonPager.setDataList(recoverVOS);
        }
        return voCommonPager;
    }

    @Override
    public Boolean batchRemove(final List<String> ids, final String applicationName) {
        if (CollectionUtils.isEmpty(ids) || StringUtils.isBlank(applicationName)) {
            return Boolean.FALSE;
        }
        final String mongoTableName = RepositoryPathUtils.buildMongoTableName(applicationName);
        ids.forEach(id -> {
            Query query = new Query();
            query.addCriteria(new Criteria("transId").is(id));
            mongoTemplate.remove(query, mongoTableName);
        });
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateRetry(final String id, final Integer retry, final String appName) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(appName) || Objects.isNull(retry)) {
            return Boolean.FALSE;
        }
        final String mongoTableName = RepositoryPathUtils.buildMongoTableName(appName);
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(id));
        Update update = new Update();
        update.set("lastTime", DateUtils.getCurrentDateTime());
        update.set("retriedCount", retry);
        final UpdateResult updateResult = mongoTemplate.updateFirst(query, update,
                MongoAdapter.class, mongoTableName);
        if (updateResult.getModifiedCount() <= 0) {
            throw new HmilyRuntimeException("更新数据异常!");
        }
        return Boolean.TRUE;
    }

}
