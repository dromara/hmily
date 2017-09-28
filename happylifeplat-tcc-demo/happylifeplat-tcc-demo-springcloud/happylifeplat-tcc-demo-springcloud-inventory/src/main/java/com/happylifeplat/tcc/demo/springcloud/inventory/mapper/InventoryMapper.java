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

package com.happylifeplat.tcc.demo.springcloud.inventory.mapper;

import com.happylifeplat.tcc.demo.springcloud.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface InventoryMapper {


    @Update("update inventory set total_inventory =#{totalInventory}," +
            " lock_inventory= #{lockInventory} " +
            " where product_id =#{productId}  and  total_inventory >0 and lock_inventory >=0 ")
    int decrease(Inventory inventory);

    @Select("select * from inventory where product_id =#{productId}")
    Inventory findByProductId(Integer productId);
}
