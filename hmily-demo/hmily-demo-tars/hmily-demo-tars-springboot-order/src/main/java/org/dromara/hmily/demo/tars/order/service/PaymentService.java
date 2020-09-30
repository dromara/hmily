package org.dromara.hmily.demo.tars.order.service;

import org.dromara.hmily.demo.common.order.entity.Order;

/**
 * @Author tydhot
 */
public interface PaymentService {

    void makePayment(Order order);

}
