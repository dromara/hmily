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

import com.happylifeplat.tcc.common.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.common.bean.entity.TccTransaction;
import com.happylifeplat.tcc.common.enums.TccActionEnum;
import com.happylifeplat.tcc.core.service.TccTransactionHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author xiaoyu
 */
@Component
public class StartTccTransactionHandler implements TccTransactionHandler {


    private final TccTransactionManager tccTransactionManager;


    private static final Lock LOCK = new ReentrantLock();

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
            LOCK.lock();
            final TccTransaction tccTransaction = tccTransactionManager.begin(point);
            try {
                //发起调用 执行try方法
                returnValue = point.proceed();
                tccTransactionManager.updateStatus(tccTransaction.getTransId(),
                        TccActionEnum.TRYING.getCode());

            } catch (Throwable throwable) {
                //异常执行cancel
                tccTransactionManager.cancel();

                throw throwable;
            }
            //try成功执行confirm confirm 失败的话，那就只能走本地补偿
            tccTransactionManager.confirm();
        } finally {
            tccTransactionManager.remove();
            LOCK.unlock();
        }
        return returnValue;
    }
}
