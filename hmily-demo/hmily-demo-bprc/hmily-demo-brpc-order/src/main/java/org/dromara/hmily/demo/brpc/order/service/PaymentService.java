/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.demo.brpc.order.service;


import org.dromara.hmily.demo.common.order.entity.Order;

/**
 * The interface Payment service.
 *
 * @author liu·yu
 */
public interface PaymentService {
    
    /**
     * 订单支付
     *
     * @param order 订单实体
     */
    void makePayment(Order order);
    
    /**
     * Make payment for tac.
     *
     * @param order the order
     */
    void makePaymentForTAC(Order order);
    
    /**
     * Test make payment.
     *
     * @param order the order
     */
    void testMakePayment(Order order);
    
    /**
     * 订单支付
     *
     * @param order 订单实体
     */
    void makePaymentWithNested(Order order);
    
    /**
     * Make payment with nested exception.
     *
     * @param order the order
     */
    void makePaymentWithNestedException(Order order);
    
    /**
     * mock订单支付的时候库存异常
     *
     * @param order 订单实体
     * @return String string
     */
    String mockPaymentInventoryWithTryException(Order order);
    
    /**
     * Mock tac payment inventory with try exception string.
     *
     * @param order the order
     * @return the string
     */
    String mockTacPaymentInventoryWithTryException(Order order);
    
    /**
     * mock订单支付的时候库存超时
     *
     * @param order 订单实体
     * @return String string
     */
    String mockPaymentInventoryWithTryTimeout(Order order);
    
    /**
     * Mock payment account with try exception string.
     *
     * @param order the order
     * @return the string
     */
    String mockPaymentAccountWithTryException(Order order);
    
    /**
     * Mock payment account with try timeout string.
     *
     * @param order the order
     * @return the string
     */
    String mockPaymentAccountWithTryTimeout(Order order);
    
    /**
     * mock订单支付的时候库存确认超时
     *
     * @param order 订单实体
     * @return String string
     */
    String mockPaymentInventoryWithConfirmTimeout(Order order);

}
