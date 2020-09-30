package org.dromara.hmily.demo.tars.inventory.service;

import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;

/**
 * @Author tydhot
 */
public interface InventoryService {

    boolean decrease(InventoryDTO inventoryDTO);

}
