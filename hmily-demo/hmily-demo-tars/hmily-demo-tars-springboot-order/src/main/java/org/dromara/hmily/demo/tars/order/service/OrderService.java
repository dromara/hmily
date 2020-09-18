package org.dromara.hmily.demo.tars.order.service;

import org.dromara.hmily.demo.common.order.entity.Order;

/**
 * @Author tydhot
 */
public interface OrderService {

    String orderPay(int count, double amount);

    Order saveOrder(int count, double amount);

}
