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

package com.hmily.tcc.demo.dubbo.order.mapper;

import com.hmily.tcc.demo.dubbo.order.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author xiaoyu
 */
public interface OrderMapper {

    /**
     * 保存订单
     *
     * @param order 订单对象
     * @return rows
     */
    @Insert(" insert into `order` (create_time,number,status,product_id,total_amount,count,user_id) " +
            " values ( #{createTime},#{number},#{status},#{productId},#{totalAmount},#{count},#{userId})")
    int save(Order order);


    /**
     * 更新订单
     *
     * @param order 订单对象
     * @return rows
     */
    @Update("update `order` set status = #{status} , total_amount=#{totalAmount} where number=#{number}")
    int update(Order order);

    /**
     * 获取所有的订单
     *
     * @return List<Order>
     */
    @Select("select * from  order")
    List<Order> listAll();
}
