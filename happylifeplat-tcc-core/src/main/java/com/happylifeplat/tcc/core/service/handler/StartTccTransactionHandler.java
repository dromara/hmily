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

package com.happylifeplat.tcc.core.service.handler;

import com.happylifeplat.tcc.core.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.core.service.TccTransactionHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class StartTccTransactionHandler implements TccTransactionHandler {


    private final TccTransactionManager tccTransactionManager;

    @Autowired
    public StartTccTransactionHandler(TccTransactionManager tccTransactionManager) {
        this.tccTransactionManager = tccTransactionManager;
    }

    /**
     * 分布式事务处理接口
     *
     * @param point   point 切点
     * @param context 信息
     * @return Object
     * @throws Throwable 异常
     */
    @Override
    public Object handler(ProceedingJoinPoint point, TccTransactionContext context) throws Throwable {
        Object returnValue;
        try {
        	//开启分布式事务
            tccTransactionManager.begin();
            try {
                //发起调用 执行try方法，进入TccCoordinatorMethodInterceptor切面
                returnValue = point.proceed();

            } catch (Throwable throwable) {
                //异常执行cancel

                tccTransactionManager.cancel();

                throw throwable;
            }
            //try成功执行confirm confirm 失败的话，那就只能走本地补偿
            tccTransactionManager.confirm();
        } finally {
            tccTransactionManager.remove();
        }
        return returnValue;
    }
}
