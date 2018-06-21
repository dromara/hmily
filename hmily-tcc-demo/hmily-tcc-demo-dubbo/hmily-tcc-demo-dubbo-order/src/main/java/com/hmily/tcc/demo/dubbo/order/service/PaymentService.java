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

package com.hmily.tcc.demo.dubbo.order.service;

import com.hmily.tcc.demo.dubbo.order.entity.Order;

/**
 * @author xiaoyu
 */
public interface PaymentService {

    /**
     * 订单支付
     *
     * @param order 订单实体
     */
    void makePayment(Order order);


    /**
     * 订单支付
     *
     * @param order 订单实体
     */
    void makePaymentWithNested(Order order);

    /**
     * mock订单支付的时候库存异常
     *
     * @param order 订单实体
     * @return String
     */
    String mockPaymentInventoryWithTryException(Order order);


    /**
     * mock订单支付的时候库存超时
     *
     * @param order 订单实体
     * @return String
     */
    String mockPaymentInventoryWithTryTimeout(Order order);


    /**
     * mock订单支付的时候库存确认异常
     *
     * @param order 订单实体
     * @return String
     */
    String mockPaymentInventoryWithConfirmException(Order order);


    /**
     * mock订单支付的时候库存确认超时
     *
     * @param order 订单实体
     * @return String
     */
    String mockPaymentInventoryWithConfirmTimeout(Order order);

}
