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

package org.dromara.hmily.demo.tars.inventory.servant.inventoryapp.impl;

import com.qq.tars.spring.annotation.TarsServant;
import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.dromara.hmily.demo.tars.inventory.servant.inventoryapp.InventoryServant;
import org.dromara.hmily.demo.tars.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author tydhot
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
