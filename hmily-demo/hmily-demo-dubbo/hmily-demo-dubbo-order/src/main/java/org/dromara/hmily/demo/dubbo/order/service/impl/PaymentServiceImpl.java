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

package org.dromara.hmily.demo.dubbo.order.service.impl;


import org.dromara.hmily.annotation.HmilyTAC;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.demo.dubbo.account.api.dto.AccountDTO;
import org.dromara.hmily.demo.dubbo.account.api.dto.AccountNestedDTO;
import org.dromara.hmily.demo.dubbo.account.api.entity.AccountDO;
import org.dromara.hmily.demo.dubbo.account.api.service.AccountService;
import org.dromara.hmily.demo.dubbo.inventory.api.dto.InventoryDTO;
import org.dromara.hmily.demo.dubbo.inventory.api.service.InventoryService;
import org.dromara.hmily.demo.dubbo.order.entity.Order;
import org.dromara.hmily.demo.dubbo.order.enums.OrderStatusEnum;
import org.dromara.hmily.demo.dubbo.order.mapper.OrderMapper;
import org.dromara.hmily.demo.dubbo.order.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xiaoyu
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final OrderMapper orderMapper;

    private final AccountService accountService;

    private final InventoryService inventoryService;

    @Autowired(required = false)
    public PaymentServiceImpl(OrderMapper orderMapper,
                              AccountService accountService,
                              InventoryService inventoryService) {
        this.orderMapper = orderMapper;
        this.accountService = accountService;
        this.inventoryService = inventoryService;
    }


    @Override
    @HmilyTCC(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public void makePayment(Order order) {
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);
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
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountService.payment(accountDTO);
        //进入扣减库存操作
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryService.decrease(inventoryDTO);
    }
    
    @Override
    @HmilyTAC
    public void makePaymentForTAC(Order order) {
        order.setStatus(OrderStatusEnum.PAY_SUCCESS.getCode());
        orderMapper.update(order);
        //扣除用户余额
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountService.paymentTAC(accountDTO);
        //进入扣减库存操作
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryService.decreaseTAC(inventoryDTO);
    }
    
    @Override
    public void testMakePayment(Order order) {
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);
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
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountService.testPayment(accountDTO);
        //进入扣减库存操作
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryService.testDecrease(inventoryDTO);
    }

    /**
     * 订单支付
     *
     * @param order 订单实体
     */
    @Override
    @HmilyTCC(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public void makePaymentWithNested(Order order) {
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);

        //做库存和资金账户的检验工作 这里只是demo 。。。
        final AccountDO accountDO = accountService.findByUserId(order.getUserId());
        if (accountDO.getBalance().compareTo(order.getTotalAmount()) <= 0) {
            throw new HmilyRuntimeException("余额不足！");
        }
        //扣除用户余额
        AccountNestedDTO accountDTO = new AccountNestedDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountDTO.setProductId(order.getProductId());
        accountDTO.setCount(order.getCount());
        accountService.paymentWithNested(accountDTO);
        //进入扣减库存操作
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryService.decrease(inventoryDTO);
    }
    
    @Override
    @HmilyTCC(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public void makePaymentWithNestedException(Order order) {
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);
    
        //做库存和资金账户的检验工作 这里只是demo 。。。
        final AccountDO accountDO = accountService.findByUserId(order.getUserId());
        if (accountDO.getBalance().compareTo(order.getTotalAmount()) <= 0) {
            throw new HmilyRuntimeException("余额不足！");
        }
        //扣除用户余额
        AccountNestedDTO accountDTO = new AccountNestedDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountDTO.setProductId(order.getProductId());
        accountDTO.setCount(order.getCount());
        accountService.paymentWithNestedException(accountDTO);
        //进入扣减库存操作
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryService.decrease(inventoryDTO);
    }
    
    @Override
    @HmilyTCC(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public String mockPaymentInventoryWithTryException(Order order) {
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);

        //扣除用户余额
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountService.payment(accountDTO);


        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryService.mockWithTryException(inventoryDTO);
        return "success";
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public String mockPaymentInventoryWithTryTimeout(Order order) {
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);

        //扣除用户余额
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountService.payment(accountDTO);

        //进入扣减库存操作
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryService.mockWithTryTimeout(inventoryDTO);
        return "success";
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public String mockPaymentInventoryWithConfirmException(Order order) {
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);

        //扣除用户余额
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountService.payment(accountDTO);


        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryService.mockWithConfirmException(inventoryDTO);
        return "success";
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public String mockPaymentInventoryWithConfirmTimeout(Order order) {
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);

        //扣除用户余额
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountService.payment(accountDTO);
        inventoryService.mockWithConfirmTimeout(new InventoryDTO());
        return "success";
    }

    public void confirmOrderStatus(Order order) {
        order.setStatus(OrderStatusEnum.PAY_SUCCESS.getCode());
        orderMapper.update(order);
        LOGGER.info("=========进行订单confirm操作完成================");
    }

    public void cancelOrderStatus(Order order) {

        order.setStatus(OrderStatusEnum.PAY_FAIL.getCode());
        orderMapper.update(order);
        LOGGER.info("=========进行订单cancel操作完成================");
    }
}
