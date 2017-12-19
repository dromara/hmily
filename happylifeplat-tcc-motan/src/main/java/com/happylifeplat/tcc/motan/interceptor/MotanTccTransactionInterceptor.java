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

package com.happylifeplat.tcc.motan.interceptor;

import com.happylifeplat.tcc.common.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.common.constant.CommonConstant;
import com.happylifeplat.tcc.common.utils.GsonUtils;
import com.happylifeplat.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.happylifeplat.tcc.core.interceptor.TccTransactionInterceptor;
import com.happylifeplat.tcc.core.service.TccTransactionAspectService;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.RpcContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * @author xiaoyu
 */
@Component
public class MotanTccTransactionInterceptor implements TccTransactionInterceptor {

    private final TccTransactionAspectService tccTransactionAspectService;

    @Autowired
    public MotanTccTransactionInterceptor(TccTransactionAspectService tccTransactionAspectService) {
        this.tccTransactionAspectService = tccTransactionAspectService;
    }


    @Override
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        TccTransactionContext tccTransactionContext = null;

        final Request request = RpcContext.getContext().getRequest();
        if (Objects.nonNull(request)) {
            final Map<String, String> attachments = request.getAttachments();
            if (attachments != null && !attachments.isEmpty()) {
                String context = attachments.get(CommonConstant.TCC_TRANSACTION_CONTEXT);
                tccTransactionContext =
                        GsonUtils.getInstance().fromJson(context, TccTransactionContext.class);
            }
        } else {
            tccTransactionContext = TransactionContextLocal.getInstance().get();
        }

        return tccTransactionAspectService.invoke(tccTransactionContext, pjp);
    }
}
