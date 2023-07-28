/*
 * Copyright 2017-2021 Dromara.org

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

package org.dromara.hmily.demo.springcloud.inventory.service.impl;

import org.dromara.hmily.annotation.HmilyTAC;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.dromara.hmily.demo.common.inventory.entity.InventoryDO;
import org.dromara.hmily.demo.common.inventory.mapper.InventoryMapper;
import org.dromara.hmily.demo.springcloud.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * InventoryServiceImpl.
 *
 * @author zhangzhi
 */
@Service("inventoryService")
public class InventoryServiceImpl implements InventoryService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryMapper inventoryMapper;

    @Autowired(required = false)
    public InventoryServiceImpl(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }


    @Override
    @HmilyTAC
    public Boolean decrease(InventoryDTO inventoryDTO) {
        LOGGER.info("==========try扣减库存decrease===========");
        inventoryMapper.decrease(inventoryDTO);
        return true;
    }
    
    @Override
    public Boolean testDecrease(InventoryDTO inventoryDTO) {
        inventoryMapper.testDecrease(inventoryDTO);
        return true;
    }
    
    @Override
    public InventoryDO findByProductId(String productId) {
        return inventoryMapper.findByProductId(productId);
    }

    @Override
    @HmilyTAC
    public Boolean mockWithTryException(InventoryDTO inventoryDTO) {
        //这里是模拟异常所以就直接抛出异常了
        throw new HmilyRuntimeException("库存扣减异常！");
    }

    @Override
    @HmilyTAC
    @Transactional(rollbackFor = Exception.class)
    public Boolean mockWithTryTimeout(InventoryDTO inventoryDTO) {
        try {
            //模拟延迟 当前线程暂停10秒
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("==========springcloud调用扣减库存mockWithTryTimeout===========");
        final int decrease = inventoryMapper.decrease(inventoryDTO);
        if (decrease != 1) {
            throw new HmilyRuntimeException("库存不足");
        }
        return true;
    }

    @Override
    @HmilyTAC
    @Transactional(rollbackFor = Exception.class)
    public Boolean mockWithConfirmTimeout(InventoryDTO inventoryDTO) {
        LOGGER.info("==========调用扣减库存try mockWithConfirmTimeout===========");
        final int decrease = inventoryMapper.decrease(inventoryDTO);
        if (decrease != 1) {
            throw new HmilyRuntimeException("库存不足");
        }
        return true;
    }
}
