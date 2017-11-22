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
package com.happylifeplat.tcc.core.interceptor;


import com.happylifeplat.tcc.annotation.Tcc;
import com.happylifeplat.tcc.annotation.TccPatternEnum;
import com.happylifeplat.tcc.common.enums.TccActionEnum;
import com.happylifeplat.tcc.common.bean.entity.Participant;
import com.happylifeplat.tcc.common.bean.entity.TccInvocation;
import com.happylifeplat.tcc.common.bean.entity.TccTransaction;
import com.happylifeplat.tcc.core.service.handler.TccTransactionManager;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;


/**
 * @author xiaoyu
 */
@Component
public class TccCoordinatorMethodInterceptor {


    private final TccTransactionManager tccTransactionManager;

    @Autowired
    public TccCoordinatorMethodInterceptor(TccTransactionManager tccTransactionManager) {
        this.tccTransactionManager = tccTransactionManager;
    }


    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {

        final TccTransaction currentTransaction = tccTransactionManager.getCurrentTransaction();

        if (Objects.nonNull(currentTransaction)) {
            final TccActionEnum action = TccActionEnum.acquireByCode(currentTransaction.getStatus());
            switch (action) {
                case PRE_TRY:
                    registerParticipant(pjp, currentTransaction.getTransId());
                    break;
                case TRYING:
                    break;
                case CONFIRMING:
                    break;
                case CANCELING:
                    break;
                default:
                    break;
            }
        }
        return pjp.proceed(pjp.getArgs());
    }


    /**
     * 获取调用接口的协调方法并封装
     *
     * @param point 切点
     */
    private void registerParticipant(ProceedingJoinPoint point, String transId) throws NoSuchMethodException {

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        Class<?> clazz = point.getTarget().getClass();

        Object[] args = point.getArgs();

        final Tcc tcc = method.getAnnotation(Tcc.class);

        //获取协调方法
        String confirmMethodName = tcc.confirmMethod();

        String cancelMethodName = tcc.cancelMethod();

        //设置模式
        final TccPatternEnum pattern = tcc.pattern();

        tccTransactionManager.getCurrentTransaction().setPattern(pattern.getCode());


        TccInvocation confirmInvocation = null;
        if (StringUtils.isNoneBlank(confirmMethodName)) {
            confirmInvocation = new TccInvocation(clazz,
                    confirmMethodName, method.getParameterTypes(), args);
        }

        TccInvocation cancelInvocation = null;
        if (StringUtils.isNoneBlank(cancelMethodName)) {
            cancelInvocation = new TccInvocation(clazz,
                    cancelMethodName,
                    method.getParameterTypes(), args);
        }


        //封装调用点
        final Participant participant = new Participant(
                transId,
                confirmInvocation,
                cancelInvocation);

        tccTransactionManager.enlistParticipant(participant);

    }
}
