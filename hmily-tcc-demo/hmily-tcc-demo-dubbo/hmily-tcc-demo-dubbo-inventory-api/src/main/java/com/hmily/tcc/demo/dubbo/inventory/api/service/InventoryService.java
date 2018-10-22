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

package com.hmily.tcc.demo.dubbo.inventory.api.service;

import com.hmily.tcc.annotation.Tcc;
import com.hmily.tcc.demo.dubbo.inventory.api.dto.InventoryDTO;
import com.hmily.tcc.demo.dubbo.inventory.api.entity.InventoryDO;

/**
 * The interface Inventory service.
 *
 * @author xiaoyu
 */
public interface InventoryService {


    /**
     * 扣减库存操作
     * 这一个tcc接口
     *
     * @param inventoryDTO 库存DTO对象
     * @return true boolean
     */
    @Tcc
    Boolean decrease(InventoryDTO inventoryDTO);


    /**
     * Test decrease boolean.
     *
     * @param inventoryDTO the inventory dto
     * @return the boolean
     */
    Boolean testDecrease(InventoryDTO inventoryDTO);

    /**
     * 获取商品库存信息
     *
     * @param productId 商品id
     * @return InventoryDO inventory do
     */
    InventoryDO findByProductId(String productId);


    /**
     * mock扣减库存异常
     *
     * @param inventoryDTO dto对象
     * @return String string
     */
    @Tcc
    String mockWithTryException(InventoryDTO inventoryDTO);


    /**
     * mock扣减库存超时
     *
     * @param inventoryDTO dto对象
     * @return String boolean
     */
    @Tcc
    Boolean mockWithTryTimeout(InventoryDTO inventoryDTO);


    /**
     * mock 扣减库存confirm异常
     *
     * @param inventoryDTO dto对象
     * @return String string
     */
    @Tcc
    String mockWithConfirmException(InventoryDTO inventoryDTO);


    /**
     * mock 扣减库存confirm超时
     *
     * @param inventoryDTO dto对象
     * @return True boolean
     */
    @Tcc
    Boolean mockWithConfirmTimeout(InventoryDTO inventoryDTO);


}
