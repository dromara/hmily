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


import com.happylifeplat.tcc.common.utils.GsonUtils;
import com.happylifeplat.tcc.core.bean.Constant;
import com.happylifeplat.tcc.core.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TccRestTemplateInterceptor implements RequestInterceptor {



    @Override
    public void apply(RequestTemplate requestTemplate) {
        final TccTransactionContext tccTransactionContext =
                TransactionContextLocal.getInstance().get();
        requestTemplate.header(Constant.TCC_TRANSACTION_CONTEXT,
                GsonUtils.getInstance().toJson(tccTransactionContext));
    }

}
