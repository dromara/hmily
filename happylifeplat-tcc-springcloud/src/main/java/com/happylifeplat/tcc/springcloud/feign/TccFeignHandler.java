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
package com.happylifeplat.tcc.springcloud.feign;

import com.happylifeplat.tcc.annotation.Tcc;
import com.happylifeplat.tcc.annotation.TccPatternEnum;
import com.happylifeplat.tcc.common.enums.TccActionEnum;
import com.happylifeplat.tcc.common.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.common.bean.entity.Participant;
import com.happylifeplat.tcc.common.bean.entity.TccInvocation;
import com.happylifeplat.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.happylifeplat.tcc.core.helper.SpringBeanUtils;
import com.happylifeplat.tcc.core.service.handler.TccTransactionManager;
import feign.InvocationHandlerFactory.MethodHandler;
import feign.Target;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * @author xiaoyu
 */
public class TccFeignHandler implements InvocationHandler {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TccFeignHandler.class);

    private Target<?> target;
    private Map<Method, MethodHandler> handlers;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        } else {

            final Tcc tcc = method.getAnnotation(Tcc.class);
            if (Objects.isNull(tcc)) {
                return this.handlers.get(method).invoke(args);
            }
            try {
                final TccTransactionManager tccTransactionManager =
                        SpringBeanUtils.getInstance().getBean(TccTransactionManager.class);
                final Object invoke = this.handlers.get(method).invoke(args);
                final Participant participant = buildParticipant(tcc, method, args, tccTransactionManager);
                if (Objects.nonNull(participant)) {
                    tccTransactionManager.enlistParticipant(participant);
                }
                return invoke;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw throwable;
            }


        }
    }


    private Participant buildParticipant(Tcc tcc, Method method, Object[] args,
                                         TccTransactionManager tccTransactionManager) {

        final TccTransactionContext tccTransactionContext =
                TransactionContextLocal.getInstance().get();
        Participant participant=null;
        if (Objects.nonNull(tccTransactionContext)) {

            if (TccActionEnum.TRYING.getCode() == tccTransactionContext.getAction()) {
                //获取协调方法
                String confirmMethodName = tcc.confirmMethod();

                if (StringUtils.isBlank(confirmMethodName)) {
                    confirmMethodName = method.getName();
                }

                String cancelMethodName = tcc.cancelMethod();

                if (StringUtils.isBlank(cancelMethodName)) {
                    cancelMethodName = method.getName();
                }

                //设置模式
                final TccPatternEnum pattern = tcc.pattern();

                tccTransactionManager.getCurrentTransaction().setPattern(pattern.getCode());

                final Class<?> declaringClass = method.getDeclaringClass();

                TccInvocation confirmInvocation = new TccInvocation(declaringClass,
                        confirmMethodName,
                        method.getParameterTypes(), args);

                TccInvocation cancelInvocation = new TccInvocation(declaringClass,
                        cancelMethodName,
                        method.getParameterTypes(), args);

                //封装调用点
                participant = new Participant(
                        tccTransactionContext.getTransId(),
                        confirmInvocation,
                        cancelInvocation);

                return participant;
            }
        }
        return participant;
    }


    public void setTarget(Target<?> target) {
        this.target = target;
    }


    public void setHandlers(Map<Method, MethodHandler> handlers) {
        this.handlers = handlers;
    }

}
