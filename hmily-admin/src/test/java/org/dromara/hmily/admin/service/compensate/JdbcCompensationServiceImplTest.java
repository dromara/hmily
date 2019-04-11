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

import org.dromara.hmily.admin.page.CommonPager;
import org.dromara.hmily.admin.page.PageParameter;
import org.dromara.hmily.admin.query.CompensationQuery;
import org.dromara.hmily.admin.service.CompensationService;
import org.dromara.hmily.admin.vo.HmilyCompensationVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * <p>Description:</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/20 16:01
 * @since JDK 1.8
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JdbcCompensationServiceImplTest {

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcCompensationServiceImplTest.class);


    @Autowired
    private CompensationService compensationService;

    @Test
    public void listByPage() throws Exception {
        CompensationQuery query = new CompensationQuery();

        PageParameter pageParameter = new PageParameter(1,10);

        query.setPageParameter(pageParameter);
        query.setApplicationName("account-service");

        final CommonPager<HmilyCompensationVO> pager = compensationService.listByPage(query);

        Assert.assertNotNull(pager.getDataList());


    }

}