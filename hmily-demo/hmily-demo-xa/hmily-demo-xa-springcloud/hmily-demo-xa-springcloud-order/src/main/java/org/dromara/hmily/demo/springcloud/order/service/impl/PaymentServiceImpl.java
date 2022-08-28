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
package org.dromara.hmily.demo.springcloud.order.service.impl;

import org.dromara.hmily.annotation.HmilyXA;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.common.account.dto.AccountNestedDTO;
import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.dromara.hmily.demo.common.order.entity.Order;
import org.dromara.hmily.demo.common.order.enums.OrderStatusEnum;
import org.dromara.hmily.demo.common.order.mapper.OrderMapper;
import org.dromara.hmily.demo.springcloud.order.client.AccountClient;
import org.dromara.hmily.demo.springcloud.order.client.InventoryClient;
import org.dromara.hmily.demo.springcloud.order.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author xiaoyu
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final OrderMapper orderMapper;

    private final AccountClient accountClient;

    private final InventoryClient inventoryClient;

    @Autowired(required = false)
    public PaymentServiceImpl(OrderMapper orderMapper,
                              AccountClient accountService,
                              InventoryClient inventoryService) {
        this.orderMapper = orderMapper;
        this.accountClient = accountService;
        this.inventoryClient = inventoryService;
    }

    @Override
    @HmilyXA
    public void makePayment(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        //做库存和资金账户的检验工作 这里只是demo 。。。
       /* final AccountDO accountDO = accountService.findByUserId(order.getUserId());
        if (accountDO.getBalance().compareTo(order.getTotalAmount()) <= 0) {
            throw new HmilyRuntimeException("余额不足！");
        }
        final InventoryDO inventory = inventoryService.findByProductId(order.getProductId());

        if (inventory.getTotalInventory() < order.getCount()) {
            throw new HmilyRuntimeException("库存不足！");
        }*/
        //扣除用户余额
        accountClient.payment(buildAccountDTO(order));
        //进入扣减库存操作
        inventoryClient.decrease(buildInventoryDTO(order));
    }

    @Override
    @HmilyXA
    @Transactional
    public void testMakePayment(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        //扣除用户余额
        System.out.println("扣除用户余额");
        accountClient.testPayment(buildAccountDTO(order));
        //进入扣减库存操作
        System.out.println ("进入扣减库存操作");
        inventoryClient.decrease(buildInventoryDTO(order));
    }

    /**
     * 订单支付
     *
     * @param order 订单实体
     */
    @Override
    public void makePaymentWithNested(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        BigDecimal decimal = accountClient.findByUserId(order.getUserId());
        if (decimal.compareTo(order.getTotalAmount()) <= 0) {
            throw new HmilyRuntimeException("余额不足！");
        }
        //扣除用户余额
        accountClient.paymentWithNested(buildAccountNestedDTO(order));
    }

    @Override
    public void makePaymentWithNestedException(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        BigDecimal decimal = accountClient.findByUserId(order.getUserId());
        if (decimal.compareTo(order.getTotalAmount()) <= 0) {
            throw new HmilyRuntimeException("余额不足！");
        }
        //扣除用户余额
        accountClient.paymentWithNestedException(buildAccountNestedDTO(order));
    }

    @Override
    public void makePaymentWithNestedTimeout(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        BigDecimal decimal = accountClient.findByUserId(order.getUserId());
        if (decimal.compareTo(order.getTotalAmount()) <= 0) {
            throw new HmilyRuntimeException("余额不足！");
        }
        //扣除用户余额
        accountClient.paymentWithNestedTimeout(buildAccountNestedDTO(order));
    }

    @Override
    @HmilyXA
    @Transactional
    public String mockPaymentInventoryWithTryException(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        //扣除用户余额
        accountClient.payment(buildAccountDTO(order));
        inventoryClient.mockWithTryException(buildInventoryDTO(order));
        return "success";
    }

    @Override
    @HmilyXA
    @Transactional
    public String mockPaymentInventoryWithTryTimeout(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        //扣除用户余额
        accountClient.payment(buildAccountDTO(order));
        inventoryClient.mockWithTryTimeout(buildInventoryDTO(order));
        return "success";
    }

    @Override
    @HmilyXA
    @Transactional
    public String mockPaymentAccountWithTryException(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
//        accountService.mockTryPaymentException(buildAccountDTO(order));
        return "success";
    }

    @Override
    @HmilyXA
    public String mockPaymentAccountWithTryTimeout(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
//        accountService.mockTryPaymentTimeout(buildAccountDTO(order));
        return "success";
    }

    private void updateOrderStatus(Order order, OrderStatusEnum orderStatus) {
        order.setStatus(orderStatus.getCode());
        orderMapper.update(order);
    }

    private AccountDTO buildAccountDTO(Order order) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        return accountDTO;
    }

    private AccountNestedDTO buildAccountNestedDTO(Order order) {
        AccountNestedDTO nestedDTO = new AccountNestedDTO();
        nestedDTO.setAmount(order.getTotalAmount());
        nestedDTO.setUserId(order.getUserId());
        nestedDTO.setProductId(order.getProductId());
        nestedDTO.setCount(order.getCount());
        return nestedDTO;
    }

    private InventoryDTO buildInventoryDTO(Order order) {
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        return inventoryDTO;
    }
}
