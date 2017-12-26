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

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.happylifeplat.tcc.common.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.core.helper.SpringBeanUtils;
import com.happylifeplat.tcc.core.service.TccTransactionAspectService;
import com.happylifeplat.tcc.core.service.TccTransactionFactoryService;
import com.happylifeplat.tcc.core.service.TccTransactionHandler;


/**
 * @author xiaoyu
 */
@Service("tccTransactionAspectService")
public class TccTransactionAspectServiceImpl implements TccTransactionAspectService {

    private final TccTransactionFactoryService tccTransactionFactoryService;

    @Autowired
    public TccTransactionAspectServiceImpl(TccTransactionFactoryService tccTransactionFactoryService) {
        this.tccTransactionFactoryService = tccTransactionFactoryService;
    }

    /**
     * tcc 事务切面服务
     *
     * @param tccTransactionContext tcc事务上下文对象
     * @param point                 切点
     * @return object
     * @throws Throwable 异常信息
     */
    @Override
    public Object invoke(TccTransactionContext tccTransactionContext, ProceedingJoinPoint point) throws Throwable {
        final Class<?> aClass = tccTransactionFactoryService.factoryOf(tccTransactionContext);
        final TccTransactionHandler txTransactionHandler =
                (TccTransactionHandler) SpringBeanUtils.getInstance().getBean(aClass);
        return txTransactionHandler.handler(point, tccTransactionContext);
    }
}
