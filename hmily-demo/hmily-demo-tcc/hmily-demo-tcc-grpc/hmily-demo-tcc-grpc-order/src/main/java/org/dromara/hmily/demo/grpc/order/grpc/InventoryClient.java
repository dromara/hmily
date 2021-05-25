package org.dromara.hmily.demo.grpc.order.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.dromara.hmily.demo.grpc.inventory.service.InventoryRequest;
import org.dromara.hmily.demo.grpc.inventory.service.InventoryResponse;
import org.dromara.hmily.demo.grpc.inventory.service.InventoryServiceGrpc;
import org.dromara.hmily.grpc.client.GrpcHmilyClient;
import org.dromara.hmily.grpc.filter.GrpcHmilyTransactionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author tydhot
 */
@Component
public class InventoryClient {

    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceBlockingStub;

    @Autowired
    GrpcHmilyClient grpcHmilyClient;

    @PostConstruct
    private void init() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 28079)
                .intercept(new GrpcHmilyTransactionFilter()).usePlaintext().build();
        inventoryServiceBlockingStub = InventoryServiceGrpc.newBlockingStub(managedChannel);
    }

    public boolean decrease(String productId, Integer count) {
        InventoryRequest request = InventoryRequest.newBuilder().setProductId(productId).setCount(count).build();

        InventoryResponse response = grpcHmilyClient.syncInvoke(inventoryServiceBlockingStub, "decrease", request, InventoryResponse.class);

        return response.getResult();
    }

}
