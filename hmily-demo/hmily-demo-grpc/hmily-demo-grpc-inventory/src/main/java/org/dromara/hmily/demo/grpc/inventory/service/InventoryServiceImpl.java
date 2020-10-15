package org.dromara.hmily.demo.grpc.inventory.service;

import io.grpc.stub.StreamObserver;
import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.dromara.hmily.demo.common.inventory.mapper.InventoryMapper;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tydhot
 */
@GRpcService
public class InventoryServiceImpl extends InventoryServiceGrpc.InventoryServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryMapper inventoryMapper;

    private static AtomicInteger confirmCount = new AtomicInteger(0);

    @Autowired(required = false)
    public InventoryServiceImpl(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public void decrease(InventoryRequest inventoryRequest, StreamObserver<InventoryResponse> streamObserver) {
//        throw new HmilyRuntimeException("库存扣减异常！");
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setProductId(inventoryRequest.getProductId());
        inventoryDTO.setCount(inventoryRequest.getCount());
        InventoryResponse response = InventoryResponse.newBuilder().setResult(inventoryMapper.decrease(inventoryDTO) > 0).build();

        streamObserver.onNext(response);
        streamObserver.onCompleted();
    }

    public Boolean confirmMethod(InventoryRequest inventoryRequest) {
        LOGGER.info("==========调用扣减库存confirm方法===========");
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setProductId(inventoryRequest.getProductId());
        inventoryDTO.setCount(inventoryDTO.getCount());
        inventoryMapper.confirm(inventoryDTO);
        final int i = confirmCount.incrementAndGet();
        LOGGER.info("调用了inventory confirm " + i + " 次");
        return true;
    }

    public Boolean cancelMethod(InventoryRequest inventoryRequest) {
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setProductId(inventoryRequest.getProductId());
        inventoryDTO.setCount(inventoryDTO.getCount());
        LOGGER.info("==========调用扣减库存取消方法===========");
        inventoryMapper.cancel(inventoryDTO);
        return true;
    }

}
