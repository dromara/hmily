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
