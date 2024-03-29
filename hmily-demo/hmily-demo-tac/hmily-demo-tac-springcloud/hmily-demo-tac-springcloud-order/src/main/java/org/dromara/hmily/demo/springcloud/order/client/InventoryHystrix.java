/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.demo.springcloud.order.client;

import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.springframework.stereotype.Component;

/**
 * The type Inventory hystrix
 *
 * @author zhangzhi
 */
@Component
public class InventoryHystrix implements InventoryClient {

    @Override
    public Boolean decrease(InventoryDTO inventoryDTO) {
        System.out.println("inventory hystrix.......");
        return false;
    }
    
    @Override
    public Boolean testDecrease(InventoryDTO inventoryDTO) {
        System.out.println("inventory hystrix.......");
        return false;
    }
    
    @Override
    public Integer findByProductId(String productId) {
        return 0;
    }

    @Override
    public Boolean mockWithTryException(InventoryDTO inventoryDTO) {
        return false;
    }

    @Override
    public Boolean mockWithTryTimeout(InventoryDTO inventoryDTO) {
        return false;
    }

    @Override
    public Boolean mockWithConfirmTimeout(InventoryDTO inventoryDTO) {
        return false;
    }
}
