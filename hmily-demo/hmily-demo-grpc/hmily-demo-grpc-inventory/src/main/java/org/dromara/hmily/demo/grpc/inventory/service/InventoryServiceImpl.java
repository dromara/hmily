package org.dromara.hmily.demo.grpc.inventory.service;

import io.grpc.stub.StreamObserver;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.dromara.hmily.demo.grpc.inventory.InventoryServiceBean;
import org.dromara.hmily.grpc.filter.GrpcHmilyServerFilter;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tydhot
 */
@GRpcService(interceptors = {GrpcHmilyServerFilter.class})
public class InventoryServiceImpl extends InventoryServiceGrpc.InventoryServiceImplBase {

    @Autowired
    InventoryServiceBean inventoryServiceBean;
    
    @Override
    public void decrease(InventoryRequest inventoryRequest, StreamObserver<InventoryResponse> streamObserver) {
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setProductId(inventoryRequest.getProductId());
        inventoryDTO.setCount(inventoryRequest.getCount());
        try {
            InventoryResponse response = InventoryResponse.newBuilder().setResult(inventoryServiceBean.decrease(inventoryDTO)).build();

            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (Exception e) {
            streamObserver.onError(new HmilyRuntimeException());
        }
    }

}
