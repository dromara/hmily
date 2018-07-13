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
package com.hmily.tcc.demo.springcloud.order.client;

import com.hmily.tcc.annotation.Tcc;
import com.hmily.tcc.demo.springcloud.order.configuration.MyConfiguration;
import com.hmily.tcc.demo.springcloud.order.dto.InventoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author xiaoyu
 */
@FeignClient(value = "inventory-service", configuration = MyConfiguration.class)
public interface InventoryClient {

    /**
     * 库存扣减
     *
     * @param inventoryDTO 实体对象
     * @return true 成功
     */
    @Tcc
    @RequestMapping("/inventory-service/inventory/decrease")
    Boolean decrease(@RequestBody InventoryDTO inventoryDTO);



    /**
     * 获取商品库存
     *
     * @param productId 商品id
     * @return InventoryDO
     */
    @RequestMapping("/inventory-service/inventory/findByProductId")
    Integer findByProductId(@RequestParam("productId") String productId);


    /**
     * 模拟库存扣减异常
     *
     * @param inventoryDTO 实体对象
     * @return true 成功
     */
    @Tcc
    @RequestMapping("/inventory-service/inventory/mockWithTryException")
    Boolean mockWithTryException(@RequestBody InventoryDTO inventoryDTO);


    /**
     * 模拟库存扣减超时
     *
     * @param inventoryDTO 实体对象
     * @return true 成功
     */
    @Tcc
    @RequestMapping("/inventory-service/inventory/mockWithTryTimeout")
    Boolean mockWithTryTimeout(@RequestBody InventoryDTO inventoryDTO);
}
