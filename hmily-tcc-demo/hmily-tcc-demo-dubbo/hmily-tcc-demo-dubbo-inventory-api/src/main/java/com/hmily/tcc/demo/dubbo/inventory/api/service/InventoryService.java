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

package com.hmily.tcc.demo.dubbo.inventory.api.service;

import com.hmily.tcc.annotation.Tcc;
import com.hmily.tcc.demo.dubbo.inventory.api.dto.InventoryDTO;
import com.hmily.tcc.demo.dubbo.inventory.api.entity.InventoryDO;

/**
 * @author xiaoyu
 */
public interface InventoryService {


    /**
     * 扣减库存操作
     * 这一个tcc接口
     *
     * @param inventoryDTO 库存DTO对象
     * @return true
     */
    @Tcc
    Boolean decrease(InventoryDTO inventoryDTO);


    /**
     * 获取商品库存信息
     * @param productId 商品id
     * @return InventoryDO
     */
    InventoryDO findByProductId(String productId);


    /**
     * mock扣减库存异常
     *
     * @param inventoryDTO dto对象
     * @return String
     */
    @Tcc
    String mockWithTryException(InventoryDTO inventoryDTO);


    /**
     * mock扣减库存超时
     *
     * @param inventoryDTO dto对象
     * @return String
     */
    @Tcc
    Boolean mockWithTryTimeout(InventoryDTO inventoryDTO);


    /**
     * mock 扣减库存confirm异常
     *
     * @param inventoryDTO dto对象
     * @return String
     */
    @Tcc
    String mockWithConfirmException(InventoryDTO inventoryDTO);


    /**
     * mock 扣减库存confirm超时
     *
     * @param inventoryDTO dto对象
     * @return True
     */
    @Tcc
    Boolean mockWithConfirmTimeout(InventoryDTO inventoryDTO);


}
