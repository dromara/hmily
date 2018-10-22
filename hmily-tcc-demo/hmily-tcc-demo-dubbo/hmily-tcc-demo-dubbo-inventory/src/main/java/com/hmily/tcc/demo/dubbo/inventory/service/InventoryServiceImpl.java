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

package com.hmily.tcc.demo.dubbo.inventory.service;

import com.hmily.tcc.annotation.Tcc;
import com.hmily.tcc.common.exception.TccRuntimeException;
import com.hmily.tcc.demo.dubbo.inventory.api.dto.InventoryDTO;
import com.hmily.tcc.demo.dubbo.inventory.api.entity.InventoryDO;
import com.hmily.tcc.demo.dubbo.inventory.api.service.InventoryService;
import com.hmily.tcc.demo.dubbo.inventory.mapper.InventoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author xiaoyu
 */
@Service("inventoryService")
@SuppressWarnings("all")
public class InventoryServiceImpl implements InventoryService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryMapper inventoryMapper;

    @Autowired(required = false)
    public InventoryServiceImpl(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    /**
     * 扣减库存操作
     * 这一个tcc接口
     *
     * @param inventoryDTO 库存DTO对象
     * @return true
     */
    @Override
    @Tcc(confirmMethod = "confirmMethod", cancelMethod = "cancelMethod")
    @Transactional
    public Boolean decrease(InventoryDTO inventoryDTO) {
        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());
        entity.setTotalInventory(entity.getTotalInventory() - inventoryDTO.getCount());
        entity.setLockInventory(entity.getLockInventory() + inventoryDTO.getCount());
        inventoryMapper.decrease(entity);
        return true;
    }

    @Override
    public Boolean testDecrease(InventoryDTO inventoryDTO) {
        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());
        entity.setTotalInventory(entity.getTotalInventory() - inventoryDTO.getCount());
        entity.setLockInventory(0);
        inventoryMapper.decrease(entity);
        return true;
    }

    /**
     * 获取商品库存信息
     *
     * @param productId 商品id
     * @return InventoryDO
     */
    @Override
    public InventoryDO findByProductId(String productId) {
        return inventoryMapper.findByProductId(productId);
    }

    @Override
    @Tcc(confirmMethod = "confirmMethod", cancelMethod = "cancelMethod")
    public String mockWithTryException(InventoryDTO inventoryDTO) {
        //这里是模拟异常所以就直接抛出异常了
        throw new TccRuntimeException("库存扣减异常！");

    }

    @Override
    @Tcc(confirmMethod = "confirmMethod", cancelMethod = "cancelMethod")
    @Transactional(rollbackFor = Exception.class)
    public Boolean mockWithTryTimeout(InventoryDTO inventoryDTO) {
        try {
            //模拟延迟 当前线程暂停10秒
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());
        entity.setTotalInventory(entity.getTotalInventory() - inventoryDTO.getCount());
        entity.setLockInventory(entity.getLockInventory() + inventoryDTO.getCount());
        final int decrease = inventoryMapper.decrease(entity);
        if (decrease != 1) {
            throw new TccRuntimeException("库存不足");
        }
        return true;
    }

    @Override
    @Tcc(confirmMethod = "confirmMethodException", cancelMethod = "cancelMethod")
    @Transactional(rollbackFor = Exception.class)
    public String mockWithConfirmException(InventoryDTO inventoryDTO) {
        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());
        entity.setTotalInventory(entity.getTotalInventory() - inventoryDTO.getCount());
        entity.setLockInventory(entity.getLockInventory() + inventoryDTO.getCount());
        final int decrease = inventoryMapper.decrease(entity);
        if (decrease != 1) {
            throw new TccRuntimeException("库存不足");
        }
        return "success";
    }


    @Override
    @Tcc(confirmMethod = "confirmMethodTimeout", cancelMethod = "cancelMethod")
    @Transactional(rollbackFor = Exception.class)
    public Boolean mockWithConfirmTimeout(InventoryDTO inventoryDTO) {
        LOGGER.info("==========调用扣减库存确认方法mockWithConfirmTimeout===========");
        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());
        entity.setTotalInventory(entity.getTotalInventory() - inventoryDTO.getCount());
        entity.setLockInventory(entity.getLockInventory() + inventoryDTO.getCount());
        final int decrease = inventoryMapper.decrease(entity);
        if (decrease != 1) {
            throw new TccRuntimeException("库存不足");
        }
        return true;
    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmMethodTimeout(InventoryDTO inventoryDTO) {

        try {
            //模拟延迟 当前线程暂停11秒
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("==========调用扣减库存确认方法===========");

        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());

        entity.setLockInventory(entity.getLockInventory() - inventoryDTO.getCount());
        inventoryMapper.decrease(entity);

        return true;

    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmMethodException(InventoryDTO inventoryDTO) {
        LOGGER.info("==========调用扣减库存确认方法===========");
        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());
        entity.setLockInventory(entity.getLockInventory() - inventoryDTO.getCount());
        final int decrease = inventoryMapper.decrease(entity);

        if (decrease != 1) {
            throw new TccRuntimeException("库存不足");
        }
        return true;
    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmMethod(InventoryDTO inventoryDTO) {
        LOGGER.info("==========调用扣减库存确认方法===========");
        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());
        entity.setLockInventory(entity.getLockInventory() - inventoryDTO.getCount());
        inventoryMapper.confirm(entity);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelMethod(InventoryDTO inventoryDTO) {

        LOGGER.info("==========调用扣减库存取消方法===========");

        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());

        entity.setTotalInventory(entity.getTotalInventory() + inventoryDTO.getCount());

        entity.setLockInventory(entity.getLockInventory() - inventoryDTO.getCount());
        inventoryMapper.cancel(entity);

        return true;

    }

}
