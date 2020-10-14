package org.dromara.hmily.demo.grpc.order.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.dromara.hmily.demo.grpc.inventory.service.InventoryRequest;
import org.dromara.hmily.demo.grpc.inventory.service.InventoryResponse;
import org.dromara.hmily.demo.grpc.inventory.service.InventoryServiceGrpc;
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
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 28079).usePlaintext().build();
        inventoryServiceBlockingStub = InventoryServiceGrpc.newBlockingStub(managedChannel);
    }

    public boolean decrease(String productId, Integer count) {
        InventoryRequest request = InventoryRequest.newBuilder().setProductId(productId).setCount(count).build();

        InventoryResponse response = inventoryServiceBlockingStub.decrease(request);

        return response.getResult();
    }

}
