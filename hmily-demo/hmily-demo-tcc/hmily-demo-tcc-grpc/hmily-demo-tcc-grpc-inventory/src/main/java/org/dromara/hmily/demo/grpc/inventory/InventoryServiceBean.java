package org.dromara.hmily.demo.grpc.inventory;

import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;

/**
 * @author tydhot
 */
public interface InventoryServiceBean {
    
    boolean decrease(InventoryDTO inventoryDTO);
    
}
