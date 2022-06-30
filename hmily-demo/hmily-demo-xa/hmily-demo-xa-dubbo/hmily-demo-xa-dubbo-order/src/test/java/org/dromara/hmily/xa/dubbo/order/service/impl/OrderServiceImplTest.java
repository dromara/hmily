package org.dromara.hmily.xa.dubbo.order.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
public class OrderServiceImplTest {

    @Autowired
    private OrderServiceImpl orderService;

    @Test
    public void testTestOrderPay() {
        orderService.testOrderPay (1, BigDecimal.valueOf (1));
    }
}
