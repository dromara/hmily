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

import org.dromara.hmily.annotation.HmilyTAC;
import org.dromara.hmily.annotation.HmilyTCC;
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

import java.math.BigDecimal;

/**
 * PaymentServiceImpl.
 *
 * @author zhangzhi
 */
@Service
@SuppressWarnings("all")
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final OrderMapper orderMapper;

    private final AccountClient accountClient;

    private final InventoryClient inventoryClient;

    @Autowired(required = false)
    public PaymentServiceImpl(OrderMapper orderMapper,
                              AccountClient accountClient,
                              InventoryClient inventoryClient) {
        this.orderMapper = orderMapper;
        this.accountClient = accountClient;
        this.inventoryClient = inventoryClient;
    }

    @Override
    @HmilyTAC
    public void makePayment(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        accountClient.payment(buildAccountDTO(order));
        inventoryClient.decrease(buildInventoryDTO(order));
    }
    
    @Override
    public void testMakePayment(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        //扣除用户余额
        accountClient.testPayment(buildAccountDTO(order));
        //进入扣减库存操作
        inventoryClient.testDecrease(buildInventoryDTO(order));
    }
    
    @Override
    @HmilyTAC
    public String mockPaymentInventoryWithTryException(Order order) {
        LOGGER.debug("===========执行springcloud  mockPaymentInventoryWithTryException 扣减资金接口==========");
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        //扣除用户余额
        accountClient.payment(buildAccountDTO(order));
        inventoryClient.mockWithTryException(buildInventoryDTO(order));
        return "success";
    }
    
    @Override
    @HmilyTAC
    public String mockPaymentAccountWithTryException(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        accountClient.mockWithTryException(buildAccountDTO(order));
        return "success";
    }
    
    @Override
    @HmilyTAC
    public String mockPaymentInventoryWithTryTimeout(Order order) {
        LOGGER.debug("===========执行springcloud  mockPaymentInventoryWithTryTimeout 扣减资金接口==========");
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        accountClient.payment(buildAccountDTO(order));
        inventoryClient.mockWithTryTimeout(buildInventoryDTO(order));
        return "success";
    }
    
    @Override
    @HmilyTAC
    public String mockPaymentAccountWithTryTimeout(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        accountClient.mockWithTryTimeout(buildAccountDTO(order));
        return "success";
    }
    
    @Override
    @HmilyTAC
    public String makePaymentWithNested(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        final BigDecimal balance = accountClient.findByUserId(order.getUserId());
        if (balance.compareTo(order.getTotalAmount()) <= 0) {
            throw new HmilyRuntimeException("余额不足！");
        }
        accountClient.paymentWithNested(buildAccountNestedDTO(order));
        return "success";
    }
    
    @Override
    @HmilyTAC
    public String makePaymentWithNestedException(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        final BigDecimal balance = accountClient.findByUserId(order.getUserId());
        if (balance.compareTo(order.getTotalAmount()) <= 0) {
            throw new HmilyRuntimeException("余额不足！");
        }
        accountClient.paymentWithNestedException(buildAccountNestedDTO(order));
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
        accountClient.payment(accountDTO);
        inventoryClient.mockWithConfirmTimeout(buildInventoryDTO(order));
        return "success";
    }

    @Override
    @HmilyTAC
    public String makePaymentWithReadCommitted(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAY_SUCCESS);
        //扣除用户余额
        accountClient.payment(buildAccountDTO(order));
        //查询账户信息, 读已提交, 此时该事务未结束, 获取全局锁失败, 将会回滚
        accountClient.findByUserId(order.getUserId());
        //进入扣减库存操作
        inventoryClient.decrease(buildInventoryDTO(order));
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
    
    private InventoryDTO buildInventoryDTO(Order order) {
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        return inventoryDTO;
    }
    
    private AccountNestedDTO buildAccountNestedDTO(Order order) {
        AccountNestedDTO nestedDTO = new AccountNestedDTO();
        nestedDTO.setAmount(order.getTotalAmount());
        nestedDTO.setUserId(order.getUserId());
        nestedDTO.setProductId(order.getProductId());
        nestedDTO.setCount(order.getCount());
        return nestedDTO;
    }
}
