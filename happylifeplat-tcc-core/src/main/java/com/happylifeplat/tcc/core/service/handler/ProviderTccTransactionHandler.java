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

import com.happylifeplat.tcc.common.enums.TccActionEnum;
import com.happylifeplat.tcc.common.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.common.bean.entity.TccTransaction;
import com.happylifeplat.tcc.core.service.TccTransactionHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author xiaoyu
 */
@Component
public class ProviderTccTransactionHandler implements TccTransactionHandler {


    private final TccTransactionManager tccTransactionManager;

    @Autowired
    public ProviderTccTransactionHandler(TccTransactionManager tccTransactionManager) {
        this.tccTransactionManager = tccTransactionManager;
    }


    /**
     * 分布式事务提供者处理接口
     * 根据tcc事务上下文的状态来执行相对应的方法
     *
     * @param point   point 切点
     * @param context context
     * @return Object
     * @throws Throwable 异常
     */
    @Override
    public Object handler(ProceedingJoinPoint point, TccTransactionContext context) throws Throwable {
        TccTransaction tccTransaction = null;
        try {
            switch (TccActionEnum.acquireByCode(context.getAction())) {
                case TRYING:
                    try {
                        //创建事务信息
                        tccTransaction = tccTransactionManager.providerBegin(context, point);
                        //发起方法调用
                        final Object proceed = point.proceed();

                        tccTransactionManager.updateStatus(tccTransaction.getTransId(),
                                TccActionEnum.TRYING.getCode());
                        return proceed;
                    } catch (Throwable throwable) {
                        tccTransactionManager.removeTccTransaction(tccTransaction);
                        throw throwable;

                    }
                case CONFIRMING:
                    //如果是confirm 通过之前保存的事务信息 进行反射调用
                    tccTransactionManager.acquire(context);
                    tccTransactionManager.confirm();
                    break;
                case CANCELING:
                    //如果是调用CANCELING 通过之前保存的事务信息 进行反射调用
                    tccTransactionManager.acquire(context);
                    tccTransactionManager.cancel();
                    break;
                default:
                    break;
            }
        } finally {
            tccTransactionManager.remove();
        }
        Method method = ((MethodSignature) (point.getSignature())).getMethod();
        return getDefaultValue(method.getReturnType());
    }

    private Object getDefaultValue(Class type) {

        if (boolean.class.equals(type)) {
            return false;
        } else if (byte.class.equals(type)) {
            return 0;
        } else if (short.class.equals(type)) {
            return 0;
        } else if (int.class.equals(type)) {
            return 0;
        } else if (long.class.equals(type)) {
            return 0;
        } else if (float.class.equals(type)) {
            return 0;
        } else if (double.class.equals(type)) {
            return 0;
        }

        return null;
    }
}
