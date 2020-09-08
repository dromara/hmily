/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.demo.common.inventory.mapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.dromara.hmily.demo.common.inventory.entity.InventoryDO;

/**
 * The interface Inventory mapper.
 *
 * @author xiaoyu
 */
public interface InventoryMapper {
    
    /**
     * Decrease int.
     *
     * @param inventoryDTO the inventory dto
     * @return the int
     */
    @Update("update inventory set total_inventory = total_inventory - #{count}," +
            " lock_inventory= lock_inventory + #{count} " +
            " where product_id =#{productId} and total_inventory > 0  ")
    int decrease(InventoryDTO inventoryDTO);
    
    /**
     * Decrease tac int.
     *
     * @param inventoryDTO the inventory dto
     * @return the int
     */
    @Update("update inventory set total_inventory = total_inventory - #{count} " +
            " where product_id =#{productId} and total_inventory > 0  ")
    int decreaseTAC(InventoryDTO inventoryDTO);
    
    /**
     * Test decrease int.
     *
     * @param inventoryDTO the inventory dto
     * @return the int
     */
    @Update("update inventory set total_inventory = total_inventory - #{count}" +
            " where product_id =#{productId} and total_inventory > 0  ")
    int testDecrease(InventoryDTO inventoryDTO);
    
    /**
     * Confirm int.
     *
     * @param inventoryDTO the inventory dto
     * @return the int
     */
    @Update("update inventory set " +
            " lock_inventory = lock_inventory - #{count} " +
            " where product_id =#{productId} and lock_inventory > 0 ")
    int confirm(InventoryDTO inventoryDTO);
    
    /**
     * Cancel int.
     *
     * @param inventoryDTO the inventory dto
     * @return the int
     */
    @Update("update inventory set total_inventory = total_inventory + #{count}," +
            " lock_inventory= lock_inventory - #{count} " +
            " where product_id =#{productId}  and lock_inventory > 0 ")
    int cancel(InventoryDTO inventoryDTO);
    
    /**
     * Find by product id inventory do.
     *
     * @param productId the product id
     * @return the inventory do
     */
    @Select("select id,product_id,total_inventory ,lock_inventory from inventory where product_id =#{productId}")
    InventoryDO findByProductId(String productId);
}
