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

package org.dromara.hmily.demo.springcloud.inventory.mapper;

import org.dromara.hmily.demo.springcloud.inventory.entity.InventoryDO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author xiaoyu
 */
@SuppressWarnings("all")
public interface InventoryMapper {

    /**
     * 库存扣减.
     *
     * @param inventory 实体对象
     * @return rows
     */
    @Update("update inventory set total_inventory =#{totalInventory}," +
            " lock_inventory= #{lockInventory} " +
            " where product_id =#{productId}  and  total_inventory >0  ")
    int decrease(InventoryDO inventory);

    /**
     * 库存扣减confirm.
     *
     * @param inventory 实体对象
     * @return rows
     */
    @Update("update inventory set " +
            " lock_inventory= #{lockInventory} " +
            " where product_id =#{productId}  and lock_inventory >0 ")
    int confirm(InventoryDO inventory);

    /**
     * 库存扣减 cancel.
     *
     * @param inventory 实体对象
     * @return rows
     */
    @Update("update inventory set total_inventory =#{totalInventory}," +
            " lock_inventory= #{lockInventory} " +
            " where product_id =#{productId}  and lock_inventory >0 ")
    int cancel(InventoryDO inventory);

    /**
     * 根据商品id找到库存信息.
     *
     * @param productId 商品id
     * @return Inventory
     */
    @Select("select * from inventory where product_id =#{productId}")
    InventoryDO findByProductId(String productId);

}
