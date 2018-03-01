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

import com.hmily.tcc.admin.page.CommonPager;
import com.hmily.tcc.admin.page.PageParameter;
import com.hmily.tcc.admin.service.CompensationService;
import com.hmily.tcc.admin.query.CompensationQuery;
import com.hmily.tcc.admin.vo.TccCompensationVO;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class ZookeeperCompensationServiceImplTest {

    @Autowired
    private CompensationService compensationService;


    @Test
    public void listByPage() throws Exception {



        CompensationQuery query = new CompensationQuery();

        query.setApplicationName("alipay-service");

        PageParameter pageParameter = new PageParameter(1,8);

        query.setPageParameter(pageParameter);

        final CommonPager<TccCompensationVO> voCommonPager = compensationService.listByPage(query);


    }

}