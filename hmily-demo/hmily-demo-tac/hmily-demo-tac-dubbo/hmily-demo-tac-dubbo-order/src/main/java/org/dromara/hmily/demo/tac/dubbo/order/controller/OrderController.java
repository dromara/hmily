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

package org.dromara.hmily.demo.tac.dubbo.order.controller;

import io.swagger.annotations.ApiOperation;
import org.dromara.hmily.demo.tac.dubbo.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * OrderController.
 * @author xiaoyu
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    
    private final OrderService orderService;

    @Autowired
    public OrderController(final OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping(value = "/orderPay")
    @ApiOperation(value = "订单支付接口（注意这里模拟的是创建订单并进行支付扣减库存等操作）")
    public String orderPay(final @RequestParam(value = "count") Integer count,
                           final @RequestParam(value = "amount") BigDecimal amount) {
        final long start = System.currentTimeMillis();
        orderService.orderPay(count, amount);
        System.out.println("消耗时间为:" + (System.currentTimeMillis() - start));
        return "";
    }
    
    @PostMapping(value = "/testOrderPay")
    @ApiOperation(value = "测试订单支付接口(这里是压测接口不添加分布式事务)")
    public String testOrderPay(final @RequestParam(value = "count") Integer count,
                               final @RequestParam(value = "amount") BigDecimal amount) {
        final long start = System.currentTimeMillis();
        orderService.testOrderPay(count, amount);
        System.out.println("消耗时间为:" + (System.currentTimeMillis() - start));
        return "";
    }
    
    @PostMapping(value = "/mockInventoryWithTryException")
    @ApiOperation(value = "模拟下单付款操作在try阶段时候，库存异常，此时账户系统和订单状态会回滚，达到数据的一致性（注意:这里模拟的是系统异常，或者rpc异常）")
    public String mockInventoryWithTryException(final @RequestParam(value = "count") Integer count,
                                                final @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.mockInventoryWithTryException(count, amount);
    }
    
    @PostMapping(value = "/mockInventoryWithTryTimeout")
    @ApiOperation(value = "模拟下单付款操作在try阶段时候，库存超时异常（但是自身最后又成功了），此时账户系统和订单状态会回滚，（库存依赖事务日志进行恢复），达到数据的一致性（异常指的是超时异常）")
    public String mockInventoryWithTryTimeout(final @RequestParam(value = "count") Integer count,
                                              final @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.mockInventoryWithTryTimeout(count, amount);
    }
    
    @PostMapping(value = "/mockAccountWithTryException")
    @ApiOperation(value = "模拟下单付款操作在try阶段时候，账户rpc异常，此时订单状态会回滚，达到数据的一致性（注意:这里模拟的是系统异常，或者rpc异常）")
    public String mockAccountWithTryException(final @RequestParam(value = "count") Integer count,
                                                final @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.mockAccountWithTryException(count, amount);
    }
    
    @PostMapping(value = "/mockAccountWithTryTimeout")
    @ApiOperation(value = "模拟下单付款操作在try阶段时候，账户rpc超时异常（但是最后自身又成功了），此时订单状态会回滚，账户系统依赖自身的事务日志进行调度恢复，达到数据的一致性（异常指的是超时异常）")
    public String mockAccountWithTryTimeout(final @RequestParam(value = "count") Integer count,
                                              final @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.mockAccountWithTryTimeout(count, amount);
    }
    
    @PostMapping(value = "/orderPayWithNested")
    @ApiOperation(value = "订单支付接口（这里模拟的是rpc的嵌套调用 order--> account--> inventory）")
    public String orderPayWithNested(final @RequestParam(value = "count") Integer count,
                                     final @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.orderPayWithNested(count, amount);
    }
    
    @PostMapping(value = "/orderPayWithNestedException")
    @ApiOperation(value = "订单支付接口（里模拟的是rpc的嵌套调用 order--> account--> inventory, inventory异常情况")
    public String orderPayWithNestedException(final @RequestParam(value = "count") Integer count,
                                     final @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.orderPayWithNestedException(count, amount);
    }

    @PostMapping(value = "/mockInventoryWithConfirmTimeout")
    @ApiOperation(value = "模拟下单付款操作中，try操作完成，但是库存模块在confirm阶段超时异常，此时订单，账户调用都会执行confirm方法，库存的confirm方法依赖自身日志，进行调度执行达到数据的一致性")
    public String mockInventoryWithConfirmTimeout(final @RequestParam(value = "count") Integer count,
                                              final @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.mockInventoryWithConfirmTimeout(count, amount);
    }

    @PostMapping(value = "/mockReadCommitted")
    @ApiOperation(value = "订单支付接口（模拟读已提交的隔离级别）")
    public String mockOrderPayWithReadCommitted(final @RequestParam(value = "count") Integer count,
                           final @RequestParam(value = "amount") BigDecimal amount) {
        final long start = System.currentTimeMillis();
        orderService.orderPayWithReadCommitted(count, amount);
        System.out.println("消耗时间为:" + (System.currentTimeMillis() - start));
        return "";
    }
}
