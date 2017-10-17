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

package com.happylifeplat.tcc.demo.springcloud.inventory.entity;

import java.io.Serializable;

/**
 * @author xiaoyu
 */
public class Inventory implements Serializable {

    private static final long serialVersionUID = 6957734749389133832L;
    private Integer id;

    /**
     * 商品id
     */
    private Integer productId;

    /**
     * 总库存
     */
    private Integer totalInventory;

    /**
     * 锁定库存
     */
    private Integer lockInventory;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getTotalInventory() {
        return totalInventory;
    }

    public void setTotalInventory(Integer totalInventory) {
        this.totalInventory = totalInventory;
    }

    public Integer getLockInventory() {
        return lockInventory;
    }

    public void setLockInventory(Integer lockInventory) {
        this.lockInventory = lockInventory;
    }
}
