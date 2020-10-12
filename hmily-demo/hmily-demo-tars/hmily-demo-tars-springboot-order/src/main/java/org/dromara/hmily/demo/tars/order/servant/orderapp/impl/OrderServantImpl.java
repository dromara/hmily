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

package org.dromara.hmily.demo.tars.order.servant.orderapp.impl;

import com.qq.tars.spring.annotation.TarsServant;
import org.dromara.hmily.demo.tars.order.servant.orderapp.OrderServant;
import org.dromara.hmily.demo.tars.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author tydhot
 */
@TarsServant("OrderObj")
public class OrderServantImpl implements OrderServant {

    private final OrderService orderService;

    @Autowired(required = false)
    public OrderServantImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String orderPay(int count, double amount) {
        return orderService.orderPay(count, amount);
    }
}
