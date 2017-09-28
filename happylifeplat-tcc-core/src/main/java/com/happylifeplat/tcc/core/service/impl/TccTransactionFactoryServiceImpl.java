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

package com.happylifeplat.tcc.core.service.impl;

import com.happylifeplat.tcc.core.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.core.service.TccTransactionFactoryService;
import com.happylifeplat.tcc.core.service.handler.ConsumeTccTransactionIHandler;
import com.happylifeplat.tcc.core.service.handler.ProviderTccTransactionHandler;
import com.happylifeplat.tcc.core.service.handler.StartTccTransactionHandler;
import com.happylifeplat.tcc.core.service.handler.TccTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service("tccTransactionFactoryService")
public class TccTransactionFactoryServiceImpl implements TccTransactionFactoryService {


    @Autowired
    private TccTransactionManager tccTransactionManager;


    /**
     * 返回 实现TxTransactionHandler类的名称
     *
     * @param context tcc事务上下文
     * @return Class<T>
     * @throws Throwable 抛出异常
     */
    @Override
    public Class factoryOf(TccTransactionContext context) throws Throwable {

        //如果事务还没开启或者 tcc事务上下文是空， 那么应该进入发起调用
        if (!tccTransactionManager.isBegin() && Objects.isNull(context)) {
            return StartTccTransactionHandler.class;
        } else if (tccTransactionManager.isBegin() && Objects.isNull(context)) {
            return ConsumeTccTransactionIHandler.class;
        } else if (Objects.nonNull(context)) {
            return ProviderTccTransactionHandler.class;
        }
        return ConsumeTccTransactionIHandler.class;
    }
}
