package org.dromara.hmily.demo.springcloud.order.client;

import org.dromara.hmily.demo.common.order.entity.Order;
import org.dromara.hmily.demo.common.order.mapper.OrderMapper;
import org.dromara.hmily.demo.springcloud.order.service.impl.OrderServiceImpl;
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
        order.setCount (100);
        order.setTotalAmount (BigDecimal.valueOf (150.54));
        order.setProductId ("100");
        order.setUserId ("12345");
        orderMapper.save (order);
    }
}