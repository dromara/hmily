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

package com.hmily.tcc.demo.dubbo.order.controller;

import com.hmily.tcc.demo.dubbo.order.service.OrderService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * @author xiaoyu
 */
@RestController
@RequestMapping("/order")
public class OrderController {


    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @PostMapping(value = "/orderPay")
    @ApiOperation(value = "订单支付接口（注意这里模拟的是创建订单并进行支付扣减库存等操作）")
    public String orderPay(@RequestParam(value = "count") Integer count,
                           @RequestParam(value = "amount") BigDecimal amount) {

        return orderService.orderPay(count, amount);

    }

    @PostMapping(value = "/testOrderPay")
    @ApiOperation(value = "测试订单支付接口(这里是压测接口不添加分布式事务)")
    public String testOrderPay(@RequestParam(value = "count") Integer count,
                               @RequestParam(value = "amount") BigDecimal amount) {

        return orderService.testOrderPay(count, amount);

    }

    @PostMapping(value = "/mockInventoryWithTryException")
    @ApiOperation(value = "模拟下单付款操作在try阶段异常，此时账户系统和订单状态会回滚，达到数据的一致性（注意:这里模拟的是系统异常，或者rpc异常）")
    public String mockInventoryWithTryException(@RequestParam(value = "count") Integer count,
                                                @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.mockInventoryWithTryException(count, amount);
    }

    @PostMapping(value = "/mockInventoryWithTryTimeout")
    @ApiOperation(value = "模拟下单付款操作在try阶段超时异常，此时账户系统和订单状态会回滚，达到数据的一致性（异常指的是超时异常）")
    public String mockInventoryWithTryTimeout(@RequestParam(value = "count") Integer count,
                                              @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.mockInventoryWithTryTimeout(count, amount);
    }


    @PostMapping(value = "/orderPayWithNested")
    @ApiOperation(value = "订单支付接口（注意这里模拟的是创建订单并进行支付扣减库存等操作）")
    public String orderPayWithNested(@RequestParam(value = "count") Integer count,
                                     @RequestParam(value = "amount") BigDecimal amount) {

        return orderService.orderPayWithNested(count, amount);

    }




    /*@PostMapping(value = "/mockInventoryWithConfirmException")
    @ApiOperation(value = "模拟下单付款操作在Confirm阶段异常，此时所有的系统调用都会执行cancel方法，达到数据的一致性（注意:这里模拟的是系统异常，或者rpc异常）")
    public String mockInventoryWithConfirmException(@RequestParam(value = "count") Integer count,
                                                @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.mockInventoryWithConfirmException(count,amount);
    }

    @PostMapping(value = "/mockInventoryWithConfirmTimeout")
    @ApiOperation(value = "模拟下单付款操作在Confirm阶段异常，此时所有的系统调用都会执行cancel方法，达到数据的一致性（异常指的是超时异常）")
    public String mockInventoryWithConfirmTimeout(@RequestParam(value = "count") Integer count,
                                              @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.mockInventoryWithConfirmTimeout(count,amount);
    }*/
}
