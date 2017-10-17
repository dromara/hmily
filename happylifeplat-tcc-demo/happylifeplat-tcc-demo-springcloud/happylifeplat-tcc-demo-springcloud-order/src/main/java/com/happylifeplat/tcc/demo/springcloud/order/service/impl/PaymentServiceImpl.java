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

package com.happylifeplat.tcc.demo.springcloud.order.service.impl;


import com.happylifeplat.tcc.annotation.Tcc;
import com.happylifeplat.tcc.demo.springcloud.order.client.AccountClient;
import com.happylifeplat.tcc.demo.springcloud.order.client.InventoryClient;
import com.happylifeplat.tcc.demo.springcloud.order.dto.AccountDTO;
import com.happylifeplat.tcc.demo.springcloud.order.dto.InventoryDTO;
import com.happylifeplat.tcc.demo.springcloud.order.entity.Order;
import com.happylifeplat.tcc.demo.springcloud.order.enums.OrderStatusEnum;
import com.happylifeplat.tcc.demo.springcloud.order.mapper.OrderMapper;
import com.happylifeplat.tcc.demo.springcloud.order.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xiaoyu
 */
@Service
public class PaymentServiceImpl implements PaymentService {


    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final OrderMapper orderMapper;


    private final AccountClient accountClient;

    private final InventoryClient inventoryClient;

    @Autowired
    public PaymentServiceImpl(OrderMapper orderMapper, AccountClient accountClient, InventoryClient inventoryClient) {
        this.orderMapper = orderMapper;
        this.accountClient = accountClient;
        this.inventoryClient = inventoryClient;
    }


    @Override
    @Tcc(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public void makePayment(Order order) {
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);
        //扣除用户余额
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());

        LOGGER.debug("===========执行springcloud扣减资金接口==========");
        accountClient.payment(accountDTO);


        //进入扣减库存操作
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryClient.decrease(inventoryDTO);
    }

    @Override
    @Tcc(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public String mockPaymentInventoryWithTryException(Order order) {

        LOGGER.debug("===========执行springcloud  mockPaymentInventoryWithTryException 扣减资金接口==========");
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);

        //扣除用户余额
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountClient.payment(accountDTO);


        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryClient.mockWithTryException(inventoryDTO);
        return "success";
    }

    @Override
    @Tcc(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public String mockPaymentInventoryWithTryTimeout(Order order) {
        LOGGER.debug("===========执行springcloud  mockPaymentInventoryWithTryTimeout 扣减资金接口==========");
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);

        //扣除用户余额
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountClient.payment(accountDTO);


        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryClient.mockWithTryTimeout(inventoryDTO);
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
