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
package com.happylifeplat.tcc.core.service;

import com.happylifeplat.tcc.core.bean.context.TccTransactionContext;
import org.aspectj.lang.ProceedingJoinPoint;

@FunctionalInterface
public interface TccTransactionHandler {

    /**
     * 分布式事务处理接口
     *
     * @param point                 point 切点
     * @param tccTransactionContext tcc事务上下文
     * @return Object
     * @throws Throwable 异常
     */
    Object handler(ProceedingJoinPoint point, TccTransactionContext tccTransactionContext) throws Throwable;
}
