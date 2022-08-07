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

package org.dromara.hmily.demo.springcloud.order.controller;

import org.dromara.hmily.demo.springcloud.order.service.OrderService;
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

    @PostMapping(value = "/testOrderPay")
    public String testOrderPay(@RequestParam(value = "count") Integer count,
                               @RequestParam(value = "amount") BigDecimal amount) {
        final long start = System.currentTimeMillis();
        String result = orderService.testOrderPay(count, amount);
        System.out.println("消耗时间为:" + (System.currentTimeMillis() - start));
        return result;
    }

    //测试其中一个事务异常
    @PostMapping(value = "/mockException")
    public String mockException(@RequestParam(value = "count") Integer count,
                                @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.mockInventoryWithTryException(count, amount);
    }

    //测试其中一个事务超时
    @PostMapping(value = "/mockTimeout")
    public String mockTimeout(@RequestParam(value = "count") Integer count,
                              @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.mockInventoryWithTryTimeout(count, amount);
    }

    //模拟rpc的嵌套调用 order--> account--> inventory,成功提交
    @PostMapping(value = "/orderPayWithNested")
    public String orderPayWithNested(@RequestParam(value = "count") Integer count,
                                     @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.orderPayWithNested(count, amount);
    }

    //模拟rpc的嵌套调用 order--> account--> inventory,发生异常
    @PostMapping(value = "/orderPayWithNestedException")
    public String orderPayWithNestedException(@RequestParam(value = "count") Integer count,
                                              @RequestParam(value = "amount") BigDecimal amount) {
        return orderService.orderPayWithNestedException(count, amount);
    }

    //模拟rpc的嵌套调用 order--> account--> inventory,发生超时
//    @PostMapping(value = "/orderPayWithNestedTimeout")
//    public String orderPayWithNestedTimeout(@RequestParam(value = "count") Integer count,
//                                              @RequestParam(value = "amount") BigDecimal amount) {
//        return orderService.orderPayWithNestedTimeout(count, amount);
//    }
}
