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

package org.dromara.hmily.demo.tac.dubbo.order.service.impl;

import org.dromara.hmily.annotation.HmilyTAC;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.demo.common.account.api.AccountService;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.common.account.dto.AccountNestedDTO;
import org.dromara.hmily.demo.common.account.entity.AccountDO;
import org.dromara.hmily.demo.common.inventory.api.InventoryService;
import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.dromara.hmily.demo.common.order.entity.Order;
import org.dromara.hmily.demo.common.order.enums.OrderStatusEnum;
import org.dromara.hmily.demo.common.order.mapper.OrderMapper;
import org.dromara.hmily.demo.tac.dubbo.order.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * PaymentServiceImpl.
 * @author xiaoyu
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final OrderMapper orderMapper;

    private final AccountService accountService;

    private final InventoryService inventoryService;

    @Autowired(required = false)
    public PaymentServiceImpl(final OrderMapper orderMapper,
                              final AccountService accountService,
                              final InventoryService inventoryService) {
        this.orderMapper = orderMapper;
        this.accountService = accountService;
        this.inventoryService = inventoryService;
    }
    
    @Override
    @HmilyTAC
    public void makePayment(final Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAY_SUCCESS);
        //扣除用户余额
        accountService.payment(buildAccountDTO(order));
        //进入扣减库存操作
        inventoryService.decrease(buildInventoryDTO(order));
    }
    
    @Override
    public void testMakePayment(final Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        //扣除用户余额
        accountService.testPayment(buildAccountDTO(order));
        //进入扣减库存操作
        inventoryService.testDecrease(buildInventoryDTO(order));
    }

    @Override
    @HmilyTAC
    public void makePaymentWithNested(final Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        final AccountDO accountDO = accountService.findByUserId(order.getUserId());
        if (accountDO.getBalance().compareTo(order.getTotalAmount()) <= 0) {
            throw new HmilyRuntimeException("余额不足！");
        }
        //扣除用户余额
        accountService.paymentWithNested(buildAccountNestedDTO(order));
    }
    
    @Override
    @HmilyTAC
    public void makePaymentWithNestedException(final Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        final AccountDO accountDO = accountService.findByUserId(order.getUserId());
        if (accountDO.getBalance().compareTo(order.getTotalAmount()) <= 0) {
            throw new HmilyRuntimeException("余额不足！");
        }
        //扣除用户余额
        accountService.paymentWithNestedException(buildAccountNestedDTO(order));
    }
    
    @Override
    @HmilyTAC
    public String mockPaymentInventoryWithTryException(final Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        //扣除用户余额
        accountService.payment(buildAccountDTO(order));
        inventoryService.mockWithTryException(buildInventoryDTO(order));
        return "success";
    }
    
    @Override
    @HmilyTAC
    public String mockPaymentInventoryWithTryTimeout(final Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        //扣除用户余额
        accountService.payment(buildAccountDTO(order));
        inventoryService.mockWithTryTimeout(buildInventoryDTO(order));
        return "success";
    }
    
    @Override
    @HmilyTAC
    public String mockPaymentAccountWithTryException(final Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        accountService.mockTryPaymentException(buildAccountDTO(order));
        return "success";
    }
    
    @Override
    @HmilyTAC
    public String mockPaymentAccountWithTryTimeout(final Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        accountService.mockTryPaymentTimeout(buildAccountDTO(order));
        return "success";
    }
    
    @Override
    @HmilyTAC
    public String mockPaymentInventoryWithConfirmTimeout(final Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        //扣除用户余额
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountService.payment(accountDTO);
        inventoryService.mockWithConfirmTimeout(buildInventoryDTO(order));
        return "success";
    }

    @Override
    @HmilyTAC
    public String makePaymentWithReadCommitted(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAY_SUCCESS);
        //扣除用户余额
        accountService.payment(buildAccountDTO(order));
        //查询账户信息, 读已提交, 此时该事务未结束, 获取全局锁失败, 将会回滚
        accountService.findByUserId(order.getUserId());
        //进入扣减库存操作
        inventoryService.decrease(buildInventoryDTO(order));
        return "success";
    }

    private void updateOrderStatus(final Order order, final OrderStatusEnum orderStatus) {
        order.setStatus(orderStatus.getCode());
        orderMapper.update(order);
    }
    
    private AccountDTO buildAccountDTO(final Order order) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        return accountDTO;
    }
    
    private AccountNestedDTO buildAccountNestedDTO(final Order order) {
        AccountNestedDTO nestedDTO = new AccountNestedDTO();
        nestedDTO.setAmount(order.getTotalAmount());
        nestedDTO.setUserId(order.getUserId());
        nestedDTO.setProductId(order.getProductId());
        nestedDTO.setCount(order.getCount());
        return nestedDTO;
    }
    
    private InventoryDTO buildInventoryDTO(final Order order) {
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        return inventoryDTO;
    }
}
