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

package com.happylifeplat.tcc.admin.service.recover;

import com.happylifeplat.tcc.admin.page.CommonPager;
import com.happylifeplat.tcc.admin.page.PageParameter;
import com.happylifeplat.tcc.admin.service.CompensationService;
import com.happylifeplat.tcc.admin.query.CompensationQuery;
import com.happylifeplat.tcc.admin.vo.TccCompensationVO;
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
public class MongoCompensationServiceImplTest {


    @Autowired
    private CompensationService compensationService;


    @Test
    public void listByPage() throws Exception {


        CompensationQuery query = new CompensationQuery();

        query.setApplicationName("alipay-service");

        PageParameter pageParameter = new PageParameter(1,5);

        query.setPageParameter(pageParameter);

        final CommonPager<TccCompensationVO> voCommonPager = compensationService.listByPage(query);


    }

}