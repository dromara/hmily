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

package com.happylifeplat.tcc.dubbo.filter;


import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.happylifeplat.tcc.annotation.Tcc;
import com.happylifeplat.tcc.annotation.TccPatternEnum;
import com.happylifeplat.tcc.common.constant.CommonConstant;
import com.happylifeplat.tcc.common.enums.TccActionEnum;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;
import com.happylifeplat.tcc.common.utils.GsonUtils;
import com.happylifeplat.tcc.common.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.common.bean.entity.Participant;
import com.happylifeplat.tcc.common.bean.entity.TccInvocation;
import com.happylifeplat.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.happylifeplat.tcc.core.service.handler.TccTransactionManager;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author xiaoyu
 */
@Activate(group = {Constants.SERVER_KEY, Constants.CONSUMER})
public class TccTransactionFilter implements Filter {


    private TccTransactionManager tccTransactionManager;

    public void setTccTransactionManager(TccTransactionManager tccTransactionManager) {
        this.tccTransactionManager = tccTransactionManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        String methodName = invocation.getMethodName();
        Class clazz = invoker.getInterface();
        Class[] args = invocation.getParameterTypes();
        final Object[] arguments = invocation.getArguments();

        Method method = null;
        Tcc tcc = null;
        try {
            method = clazz.getDeclaredMethod(methodName, args);
            tcc = method.getAnnotation(Tcc.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (Objects.nonNull(tcc)) {
            try {

                final TccTransactionContext tccTransactionContext =
                        TransactionContextLocal.getInstance().get();
                if (Objects.nonNull(tccTransactionContext)) {
                    RpcContext.getContext()
                            .setAttachment(CommonConstant.TCC_TRANSACTION_CONTEXT,
                                    GsonUtils.getInstance().toJson(tccTransactionContext));
                }

                final Result result = invoker.invoke(invocation);
                final Participant participant = buildParticipant(tccTransactionContext,tcc, method, clazz, arguments, args);
                if (Objects.nonNull(participant)) {
                    tccTransactionManager.enlistParticipant(participant);
                }

                return result;
            } catch (RpcException e) {
                e.printStackTrace();
                throw e;
            }
        } else {
            return invoker.invoke(invocation);
        }

    }

    @SuppressWarnings("unchecked")
    private Participant buildParticipant(TccTransactionContext tccTransactionContext,Tcc tcc, Method method, Class clazz, Object[] arguments, Class... args) throws TccRuntimeException {

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


                TccInvocation confirmInvocation = new TccInvocation(clazz,
                        confirmMethodName,
                        args, arguments);

                TccInvocation cancelInvocation = new TccInvocation(clazz,
                        cancelMethodName,
                        args, arguments);

                //封装调用点
                return new Participant(
                        tccTransactionContext.getTransId(),
                        confirmInvocation,
                        cancelInvocation);
            }

        }

        return null;


    }
}
