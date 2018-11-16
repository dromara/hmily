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

package org.dromara.hmily.demo.springcloud.account.mapper;

import org.dromara.hmily.demo.springcloud.account.entity.AccountDO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author xiaoyu
 */
@SuppressWarnings("all")
public interface AccountMapper {

    /**
     * 扣减账户余额
     *
     * @param accountDO 实体类
     * @return rows
     */
    @Update("update account set balance =#{balance}," +
            " freeze_amount= #{freezeAmount} ,update_time = #{updateTime}" +
            " where user_id =#{userId}  and  balance > 0 ")
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
    @Select("select * from account where user_id =#{userId}")
    AccountDO findByUserId(String userId);
}
