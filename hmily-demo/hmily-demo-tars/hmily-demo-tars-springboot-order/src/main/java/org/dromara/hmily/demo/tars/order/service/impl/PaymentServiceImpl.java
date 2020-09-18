package org.dromara.hmily.demo.tars.order.service.impl;

import com.qq.tars.spring.annotation.TarsClient;
import org.dromara.hmily.demo.common.order.entity.Order;
import org.dromara.hmily.demo.common.order.enums.OrderStatusEnum;
import org.dromara.hmily.demo.common.order.mapper.OrderMapper;
import org.dromara.hmily.demo.tars.order.servant.accountapp.AccountPrx;
import org.dromara.hmily.demo.tars.order.servant.inventoryapp.InventoryPrx;
import org.dromara.hmily.demo.tars.order.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author tydhot
 */
@Service("paymentService")
public class PaymentServiceImpl implements PaymentService{

    @TarsClient("TestInventory.InventoryApp.InventoryObj")
    InventoryPrx inventoryPrx;

    @TarsClient("HmilyAccount.AccountApp.AccountObj")
    AccountPrx accountPrx;

    private final OrderMapper orderMapper;

    @Autowired(required = false)
    public PaymentServiceImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public void makePayment(Order order) {
        updateOrderStatus(order, OrderStatusEnum.PAYING);
        accountPrx.payment(order.getUserId(), order.getTotalAmount().doubleValue());
        inventoryPrx.decrease(order.getProductId(), order.getCount());
    }

    private void updateOrderStatus(Order order, OrderStatusEnum orderStatus) {
        order.setStatus(orderStatus.getCode());
        orderMapper.update(order);
    }
}
