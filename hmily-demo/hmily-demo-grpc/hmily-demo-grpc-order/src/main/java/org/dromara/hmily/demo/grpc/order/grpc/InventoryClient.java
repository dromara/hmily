package org.dromara.hmily.demo.grpc.order.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.dromara.hmily.core.holder.SingletonHolder;
import org.dromara.hmily.demo.grpc.inventory.service.InventoryRequest;
import org.dromara.hmily.demo.grpc.inventory.service.InventoryResponse;
import org.dromara.hmily.demo.grpc.inventory.service.InventoryServiceGrpc;
import org.dromara.hmily.grpc.filter.GrpcHmilyTransactionFilter;
import org.dromara.hmily.grpc.parameter.GrpcHmilyContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author tydhot
 */
@Component
public class InventoryClient {

    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceBlockingStub;

    @PostConstruct
    private void init() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 28079)
                .intercept(new GrpcHmilyTransactionFilter()).usePlaintext().build();
        inventoryServiceBlockingStub = InventoryServiceGrpc.newBlockingStub(managedChannel);
        SingletonHolder.INST.register(InventoryServiceGrpc.InventoryServiceBlockingStub.class, inventoryServiceBlockingStub);
    }

    public boolean decrease(String productId, Integer count) {
        InventoryRequest request = InventoryRequest.newBuilder().setProductId(productId).setCount(count).build();
        GrpcHmilyContext.getHmilyParam().set(request);

        InventoryResponse response = inventoryServiceBlockingStub.decrease(request);

        return response.getResult();
    }

}
