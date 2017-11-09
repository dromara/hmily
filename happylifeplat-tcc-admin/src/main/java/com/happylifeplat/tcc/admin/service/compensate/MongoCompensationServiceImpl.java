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

package com.happylifeplat.tcc.admin.service.compensate;

import com.happylifeplat.tcc.admin.helper.PageHelper;
import com.happylifeplat.tcc.admin.page.CommonPager;
import com.happylifeplat.tcc.admin.page.PageParameter;
import com.happylifeplat.tcc.admin.service.CompensationService;
import com.happylifeplat.tcc.admin.helper.ConvertHelper;
import com.happylifeplat.tcc.admin.query.CompensationQuery;
import com.happylifeplat.tcc.admin.vo.TccCompensationVO;
import com.happylifeplat.tcc.common.bean.adapter.MongoAdapter;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;
import com.happylifeplat.tcc.common.utils.DateUtils;
import com.happylifeplat.tcc.common.utils.RepositoryPathUtils;
import com.mongodb.WriteResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>Description: .</p>
 * Mongodb 实现
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/19 17:08
 * @since JDK 1.8
 */
public class MongoCompensationServiceImpl implements CompensationService {

    private MongoTemplate mongoTemplate;


    public MongoCompensationServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCompensationServiceImpl.class);

    /**
     * 分页获取补偿事务信息
     *
     * @param query 查询条件
     * @return CommonPager<TransactionRecoverVO>
     */
    @Override
    public CommonPager<TccCompensationVO> listByPage(CompensationQuery query) {
        CommonPager<TccCompensationVO> voCommonPager = new CommonPager<>();

        final String mongoTableName = RepositoryPathUtils.buildMongoTableName(query.getApplicationName());

        final PageParameter pageParameter = query.getPageParameter();
        final int currentPage = pageParameter.getCurrentPage();
        final int pageSize = pageParameter.getPageSize();

        int start = (currentPage - 1) * pageSize;

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

        voCommonPager.setPage(PageHelper.buildPage(query.getPageParameter(), (int) totalCount));

        baseQuery.skip(start).limit(pageSize);

        final List<MongoAdapter> mongoAdapters =
                mongoTemplate.find(baseQuery, MongoAdapter.class, mongoTableName);

        if (CollectionUtils.isNotEmpty(mongoAdapters)) {
            final List<TccCompensationVO> recoverVOS =
                    mongoAdapters
                            .stream()
                            .map(ConvertHelper::buildVO)
                            .collect(Collectors.toList());
            voCommonPager.setDataList(recoverVOS);
        }

        return voCommonPager;
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
        final String mongoTableName = RepositoryPathUtils.buildMongoTableName(applicationName);

        ids.forEach(id -> {
            Query query = new Query();
            query.addCriteria(new Criteria("transId").is(id));
            mongoTemplate.remove(query, mongoTableName);
        });

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
        final String mongoTableName = RepositoryPathUtils.buildMongoTableName(applicationName);

        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(id));
        Update update = new Update();
        update.set("lastTime", DateUtils.getCurrentDateTime());
        update.set("retriedCount", retry);
        final WriteResult writeResult = mongoTemplate.updateFirst(query, update,
                MongoAdapter.class, mongoTableName);
        if (writeResult.getN() <= 0) {
            throw new TccRuntimeException("更新数据异常!");
        }
        return Boolean.TRUE;
    }


}
