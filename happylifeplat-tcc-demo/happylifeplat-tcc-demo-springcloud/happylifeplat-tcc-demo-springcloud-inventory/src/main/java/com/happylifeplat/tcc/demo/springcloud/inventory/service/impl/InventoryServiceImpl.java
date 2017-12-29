/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.happylifeplat.tcc.demo.springcloud.inventory.service.impl;

import com.happylifeplat.tcc.annotation.Tcc;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;

import com.happylifeplat.tcc.demo.springcloud.inventory.dto.InventoryDTO;
import com.happylifeplat.tcc.demo.springcloud.inventory.entity.InventoryDO;
import com.happylifeplat.tcc.demo.springcloud.inventory.mapper.InventoryMapper;
import com.happylifeplat.tcc.demo.springcloud.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author xiaoyu
 */
@Service("inventoryService")
public class InventoryServiceImpl implements InventoryService {

    /**
     * logger
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
    public Boolean decrease(InventoryDTO inventoryDTO) {
        LOGGER.info("==========springcloud调用扣减库存decrease===========");
        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());
        entity.setTotalInventory(entity.getTotalInventory() - inventoryDTO.getCount());
        entity.setLockInventory(entity.getLockInventory() + inventoryDTO.getCount());
        final int decrease = inventoryMapper.decrease(entity);
        if (decrease != 1) {
            throw new TccRuntimeException("库存不足");
        }
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
    public Boolean mockWithTryException(InventoryDTO inventoryDTO) {
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
        LOGGER.info("==========springcloud调用扣减库存mockWithTryTimeout===========");
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
        LOGGER.info("==========Springcloud调用扣减库存确认方法===========");

        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());

        entity.setLockInventory(entity.getLockInventory() - inventoryDTO.getCount());
        inventoryMapper.decrease(entity);

        return true;

    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmMethodException(InventoryDTO inventoryDTO) {

        LOGGER.info("==========Springcloud调用扣减库存确认方法===========");

        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());

        entity.setLockInventory(entity.getLockInventory() - inventoryDTO.getCount());
        final int decrease = inventoryMapper.decrease(entity);

        if (decrease != 1) {
            throw new TccRuntimeException("库存不足");
        }
        return true;

        // throw new TccRuntimeException("库存扣减确认异常！");


    }


    public Boolean confirmMethod(InventoryDTO inventoryDTO) {

        LOGGER.info("==========Springcloud调用扣减库存确认方法===========");

        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());


        entity.setLockInventory(entity.getLockInventory() - inventoryDTO.getCount());

        final int rows = inventoryMapper.confirm(entity);


        if (rows != 1) {
            throw new TccRuntimeException("确认库存操作失败！");
        }

        return true;

    }

    public Boolean cancelMethod(InventoryDTO inventoryDTO) {

        LOGGER.info("==========Springcloud调用扣减库存取消方法===========");

        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());

        entity.setTotalInventory(entity.getTotalInventory() + inventoryDTO.getCount());

        entity.setLockInventory(entity.getLockInventory() - inventoryDTO.getCount());

       int rows= inventoryMapper.cancel(entity);


        if (rows != 1) {
            throw new TccRuntimeException("取消库存操作失败！");
        }

        return true;

    }

}
