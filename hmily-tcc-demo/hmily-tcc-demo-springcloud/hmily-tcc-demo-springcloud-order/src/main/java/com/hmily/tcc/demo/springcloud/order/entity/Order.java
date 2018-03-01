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

package com.hmily.tcc.demo.springcloud.order.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xiaoyu
 */
@Data
public class Order implements Serializable {

    private static final long serialVersionUID = -8551347266419380333L;

    private Integer id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 订单编号
     */
    private String number;


    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 商品id
     */
    private String productId;

    /**
     * 付款金额
     */
    private BigDecimal totalAmount;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 购买人
     */
    private String userId;

}
