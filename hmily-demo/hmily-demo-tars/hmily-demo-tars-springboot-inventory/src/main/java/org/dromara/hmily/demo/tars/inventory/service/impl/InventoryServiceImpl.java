package org.dromara.hmily.demo.tars.inventory.service.impl;

import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.dromara.hmily.demo.common.inventory.mapper.InventoryMapper;
import org.dromara.hmily.demo.tars.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author tydhot
 */
@Service("inventoryService")
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;

    @Autowired(required = false)
    public InventoryServiceImpl(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public boolean decrease(InventoryDTO inventoryDTO) {
        return inventoryMapper.decrease(inventoryDTO) > 0;
    }
}
