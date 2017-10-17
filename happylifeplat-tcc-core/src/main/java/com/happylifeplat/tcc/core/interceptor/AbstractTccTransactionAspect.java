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


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;


/**
 * @author xiaoyu
 */
@Aspect
public abstract class AbstractTccTransactionAspect {

    private TccTransactionInterceptor tccTransactionInterceptor;

    public void setTccTransactionInterceptor(TccTransactionInterceptor tccTransactionInterceptor) {
        this.tccTransactionInterceptor = tccTransactionInterceptor;
    }

    @Pointcut("@annotation(com.happylifeplat.tcc.annotation.Tcc)")
    public void txTransactionInterceptor() {

    }

    @Around("txTransactionInterceptor()")
    public Object interceptCompensableMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return tccTransactionInterceptor.interceptor(proceedingJoinPoint);
    }

    /**
     * spring Order 接口，该值的返回直接会影响springBean的加载顺序
     *
     * @return int 类型
     */
    public abstract int getOrder();
}
