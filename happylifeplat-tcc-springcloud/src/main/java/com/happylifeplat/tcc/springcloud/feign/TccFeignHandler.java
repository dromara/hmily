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
import com.happylifeplat.tcc.core.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.core.bean.entity.Participant;
import com.happylifeplat.tcc.core.bean.entity.TccInvocation;
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
 * 动态代理类都必须要实现InvocationHandler这个接口
 * @author mqzhao
 *
 */
public class TccFeignHandler implements InvocationHandler {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TccFeignHandler.class);

    private Target<?> target;
    private Map<Method, MethodHandler> handlers;

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        } else {

            final Tcc tcc = method.getAnnotation(Tcc.class);
            if (Objects.isNull(tcc)) {
                return this.handlers.get(method).invoke(args);
            }

            final TccTransactionContext tccTransactionContext =
                    TransactionContextLocal.getInstance().get();
            if (Objects.nonNull(tccTransactionContext)) {
                final TccTransactionManager tccTransactionManager =
                        SpringBeanUtils.getInstance().getBean(TccTransactionManager.class);
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
                    final Participant participant = new Participant(
                            tccTransactionContext.getTransId(),
                            confirmInvocation,
                            cancelInvocation);

                    tccTransactionManager.enlistParticipant(participant);
                }

            }


            return this.handlers.get(method).invoke(args);
        }
    }


    public void setTarget(Target<?> target) {
        this.target = target;
    }


    public void setHandlers(Map<Method, MethodHandler> handlers) {
        this.handlers = handlers;
    }

}
