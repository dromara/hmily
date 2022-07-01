package org.dromara.hmily.xa.dubbo.order.service.impl;

import org.dromara.hmily.demo.common.order.entity.Order;
import org.dromara.hmily.demo.common.order.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
public class OrderServiceImplTest {

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Test
    public void testTestOrderPay() {
        orderService.testOrderPay (1, BigDecimal.valueOf (1));
    }

    @Test
    public void testTestOrderPay2() {
        Order order = new Order ();
        order.setCount (1);
        order.setProductId ("1");
        orderMapper.save (order);
    }
}
