/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.demo.springcloud.order.service;

import java.math.BigDecimal;

/**
 * The interface Order service.
 *
 * @author xiaoyu
 */
public interface OrderService {

    /**
     * 创建订单并且进行扣除账户余额支付，并进行库存扣减操作
     *
     * @param count  购买数量
     * @param amount 支付金额
     * @return string string
     */
    String orderPay(Integer count, BigDecimal amount);

    /**
     * Test order pay string.
     *
     * @param count  the count
     * @param amount the amount
     * @return the string
     */
    String testOrderPay(Integer count, BigDecimal amount);

    /**
     * 创建订单并且进行扣除账户余额支付，并进行库存扣减操作
     * in this  Inventory nested in account.
     *
     * @param count  购买数量
     * @param amount 支付金额
     * @return string string
     */
    String orderPayWithNested(Integer count, BigDecimal amount);

    /**
     * Order pay with nested exception string.
     *
     * @param count  the count
     * @param amount the amount
     * @return the string
     */
    String orderPayWithNestedException(Integer count, BigDecimal amount);

    String orderPayWithNestedTimeout(Integer count, BigDecimal amount);

    /**
     * 模拟在订单支付操作中，库存在try阶段中的库存异常
     *
     * @param count  购买数量
     * @param amount 支付金额
     * @return string string
     */
    String mockInventoryWithTryException(Integer count, BigDecimal amount);

    /**
     * 模拟在订单支付操作中，库存在try阶段中的timeout
     *
     * @param count  购买数量
     * @param amount 支付金额
     * @return string string
     */
    String mockInventoryWithTryTimeout(Integer count, BigDecimal amount);

    /**
     * 模拟在订单支付操作中，账户在try阶段中的异常
     *
     * @param count  购买数量
     * @param amount 支付金额
     * @return string string
     */
    String mockAccountWithTryException(Integer count, BigDecimal amount);

    /**
     * 模拟在订单支付操作中，账户在try阶段中的timeout超时异常（最后自身成功）
     *
     * @param count  购买数量
     * @param amount 支付金额
     * @return string string
     */
    String mockAccountWithTryTimeout(Integer count, BigDecimal amount);


}
