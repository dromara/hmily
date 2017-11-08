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
package com.happylifeplat.tcc.springcloud.interceptor;

import com.happylifeplat.tcc.common.constant.CommonConstant;
import com.happylifeplat.tcc.common.utils.GsonUtils;
import com.happylifeplat.tcc.common.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.core.interceptor.TccTransactionInterceptor;
import com.happylifeplat.tcc.core.service.TccTransactionAspectService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xiaoyu
 */
@Component
public class SpringCloudTxTransactionInterceptor implements TccTransactionInterceptor {

    private final TccTransactionAspectService tccTransactionAspectService;

    @Autowired
    public SpringCloudTxTransactionInterceptor(TccTransactionAspectService tccTransactionAspectService) {
        this.tccTransactionAspectService = tccTransactionAspectService;
    }


    @Override
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        TccTransactionContext tccTransactionContext;
        //如果不是本地反射调用补偿
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = requestAttributes == null ? null : ((ServletRequestAttributes) requestAttributes).getRequest();
        String context = request == null ? null : request.getHeader(CommonConstant.TCC_TRANSACTION_CONTEXT);
        tccTransactionContext =
                GsonUtils.getInstance().fromJson(context, TccTransactionContext.class);

        return tccTransactionAspectService.invoke(tccTransactionContext, pjp);
    }

}
