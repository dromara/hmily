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

package com.happylifeplat.tcc.demo.dubbo.order.service.impl;


import com.happylifeplat.tcc.annotation.Tcc;
import com.happylifeplat.tcc.demo.dubbo.account.api.dto.AccountDTO;
import com.happylifeplat.tcc.demo.dubbo.account.api.service.AccountService;
import com.happylifeplat.tcc.demo.dubbo.inventory.api.dto.InventoryDTO;
import com.happylifeplat.tcc.demo.dubbo.inventory.api.service.InventoryService;
import com.happylifeplat.tcc.demo.dubbo.order.entity.Order;
import com.happylifeplat.tcc.demo.dubbo.order.enums.OrderStatusEnum;
import com.happylifeplat.tcc.demo.dubbo.order.mapper.OrderMapper;
import com.happylifeplat.tcc.demo.dubbo.order.service.OrderService;
import com.happylifeplat.tcc.demo.dubbo.order.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {


    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);


    private final OrderMapper orderMapper;

    private final AccountService accountService;

    private final InventoryService inventoryService;

    @Autowired
    public PaymentServiceImpl(OrderMapper orderMapper, AccountService accountService, InventoryService inventoryService) {
        this.orderMapper = orderMapper;
        this.accountService = accountService;
        this.inventoryService = inventoryService;
    }


    @Tcc(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public void makePayment(Order order) {
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
        inventoryService.decrease(inventoryDTO);
    }

    @Override
    @Tcc(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
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
    @Tcc(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
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
    @Tcc(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
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
    @Tcc(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
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
