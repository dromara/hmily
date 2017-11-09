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

import com.google.common.base.Splitter;
import com.happylifeplat.tcc.admin.service.ApplicationNameService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/20 16:39
 * @since JDK 1.8
 */
@Service("recoverApplicationNameService")
public class ApplicationNameServiceImpl implements ApplicationNameService {


    @Value("${tcc.application.list}")
    private String appNameList;

    /**
     * 获取之前参与分布式事务项目的应用名称
     *
     * @return List<String>
     */
    @Override
    public List<String> list() {
        return Splitter.on(",").splitToList(appNameList);
    }
}
