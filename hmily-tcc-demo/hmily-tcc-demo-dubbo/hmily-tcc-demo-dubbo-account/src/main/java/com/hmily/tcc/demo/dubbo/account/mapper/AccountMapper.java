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

package com.hmily.tcc.demo.dubbo.account.mapper;

import com.hmily.tcc.demo.dubbo.account.api.entity.AccountDO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author xiaoyu
 */
public interface AccountMapper {

    /**
     * 扣减账户余额
     *
     * @param accountDO 实体类
     * @return rows
     */
    @Update("update account set balance =#{balance}," +
            " freeze_amount= #{freezeAmount} ,update_time = #{updateTime}" +
            " where user_id =#{userId}  and  balance > 0  ")
    int update(AccountDO accountDO);


    /**
     * 确认扣减账户余额
     *
     * @param accountDO 实体类
     * @return rows
     */
    @Update("update account set " +
            " freeze_amount= #{freezeAmount} ,update_time = #{updateTime}" +
            " where user_id =#{userId}  and freeze_amount >0 ")
    int confirm(AccountDO accountDO);


    /**
     * 取消扣减账户余额
     *
     * @param accountDO 实体类
     * @return rows
     */
    @Update("update account set balance =#{balance}," +
            " freeze_amount= #{freezeAmount} ,update_time = #{updateTime}" +
            " where user_id =#{userId}  and freeze_amount >0")
    int cancel(AccountDO accountDO);


    /**
     * 根据userId获取用户账户信息
     *
     * @param userId 用户id
     * @return AccountDO
     */
    @Select("select * from account where user_id =#{userId} for update")
    AccountDO findByUserId(String userId);
}
