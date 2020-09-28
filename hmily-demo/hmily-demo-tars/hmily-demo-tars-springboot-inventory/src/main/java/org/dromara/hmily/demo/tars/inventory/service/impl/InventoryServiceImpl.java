package org.dromara.hmily.demo.tars.inventory.service.impl;

import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.dromara.hmily.demo.common.inventory.mapper.InventoryMapper;
import org.dromara.hmily.demo.tars.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

/**
 * @Author tydhot
 */
@Service("inventoryService")
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;

    private static AtomicInteger confirmCount = new AtomicInteger(0);

    @Autowired(required = false)
    public InventoryServiceImpl(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmMethod", cancelMethod = "cancelMethod")
    public boolean decrease(InventoryDTO inventoryDTO) {
//        throw new HmilyRuntimeException("库存扣减异常！");
        return inventoryMapper.decrease(inventoryDTO) > 0;
    }

    /**
     * Confirm method boolean.
     *
     * @param inventoryDTO the inventory dto
     * @return the boolean
     */
    public Boolean confirmMethod(InventoryDTO inventoryDTO) {
        LOGGER.info("==========调用扣减库存confirm方法===========");
        inventoryMapper.confirm(inventoryDTO);
        final int i = confirmCount.incrementAndGet();
        LOGGER.info("调用了inventory confirm " + i + " 次");
        return true;
    }

    /**
     * Cancel method boolean.
     *
     * @param inventoryDTO the inventory dto
     * @return the boolean
     */
    public Boolean cancelMethod(InventoryDTO inventoryDTO) {
        LOGGER.info("==========调用扣减库存取消方法===========");
        inventoryMapper.cancel(inventoryDTO);
        return true;
    }
}
