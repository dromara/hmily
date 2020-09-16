package org.dromara.hmily.demo.tars.inventory.servant.inventoryapp.impl;

import com.qq.tars.spring.annotation.TarsServant;
import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.dromara.hmily.demo.tars.inventory.servant.inventoryapp.InventoryServant;
import org.dromara.hmily.demo.tars.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author tydhot
 */
@TarsServant("InventoryObj")
public class InventoryServantImpl implements InventoryServant {

    private final InventoryService inventoryService;

    @Autowired(required = false)
    public InventoryServantImpl(final InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public boolean decrease(String productId, int count) {
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setProductId(productId);
        inventoryDTO.setCount(count);
        return inventoryService.decrease(inventoryDTO);
    }
}
