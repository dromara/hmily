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

package org.dromara.hmily.demo.springcloud.account.service.impl;

import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.common.exception.HmilyRuntimeException;

import org.dromara.hmily.demo.springcloud.account.dto.AccountDTO;
import org.dromara.hmily.demo.springcloud.account.entity.AccountDO;
import org.dromara.hmily.demo.springcloud.account.mapper.AccountMapper;
import org.dromara.hmily.demo.springcloud.account.service.AccountService;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author xiaoyu
 */
@Service("accountService")
@SuppressWarnings("all")
public class AccountServiceImpl implements AccountService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);


    private final AccountMapper accountMapper;

    @Autowired(required = false)
    public AccountServiceImpl(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /**
     * 扣款支付
     *
     * @param accountDTO 参数dto
     * @return true
     */
    @Override
    @Hmily(confirmMethod = "confirm", cancelMethod = "cancel")
    @Transactional
    public boolean payment(AccountDTO accountDTO) {
        LOGGER.debug("============springcloud执行try付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountDO.setBalance(accountDO.getBalance().subtract(accountDTO.getAmount()));
        accountDO.setFreezeAmount(accountDO.getFreezeAmount().add(accountDTO.getAmount()));
        accountDO.setUpdateTime(new Date());

        final int update = accountMapper.update(accountDO);
        if (update != 1) {
            throw new HmilyRuntimeException("资金不足！");
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        throw new RuntimeException("");
        return Boolean.TRUE;
    }

    /**
     * 获取用户账户信息
     *
     * @param userId 用户id
     * @return AccountDO
     */
    @Override
    public AccountDO findByUserId(String userId) {
        return accountMapper.findByUserId(userId);
    }

    public boolean confirm(AccountDTO accountDTO) {

        LOGGER.debug("============springcloud tcc 执行确认付款接口===============");

        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountDO.setFreezeAmount(accountDO.getFreezeAmount().subtract(accountDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        final int rows = accountMapper.confirm(accountDO);
        if (rows != 1) {
            throw new HmilyRuntimeException("确认扣减账户异常！");
        }
        return Boolean.TRUE;
    }


    public boolean cancel(AccountDTO accountDTO) {

        LOGGER.debug("============springcloud tcc 执行取消付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountDO.setBalance(accountDO.getBalance().add(accountDTO.getAmount()));
        accountDO.setFreezeAmount(accountDO.getFreezeAmount().subtract(accountDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        final int rows = accountMapper.cancel(accountDO);
        if (rows != 1) {
            throw new HmilyRuntimeException("取消扣减账户异常！");
        }
        return Boolean.TRUE;
    }
}
