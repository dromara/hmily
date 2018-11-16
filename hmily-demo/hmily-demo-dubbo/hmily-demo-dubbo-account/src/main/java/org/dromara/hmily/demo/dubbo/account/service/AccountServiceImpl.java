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

package org.dromara.hmily.demo.dubbo.account.service;

import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.demo.dubbo.account.api.dto.AccountDTO;
import org.dromara.hmily.demo.dubbo.account.api.dto.AccountNestedDTO;
import org.dromara.hmily.demo.dubbo.account.api.entity.AccountDO;
import org.dromara.hmily.demo.dubbo.account.api.service.AccountService;
import org.dromara.hmily.demo.dubbo.account.api.service.InlineService;
import org.dromara.hmily.demo.dubbo.account.mapper.AccountMapper;
import org.dromara.hmily.demo.dubbo.inventory.api.dto.InventoryDTO;
import org.dromara.hmily.demo.dubbo.inventory.api.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

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
    private InventoryService inventoryService;

    @Autowired(required = false)
    private InlineService inlineService;

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
    public void payment(AccountDTO accountDTO) {
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountDO.setBalance(accountDO.getBalance().subtract(accountDTO.getAmount()));
        accountDO.setFreezeAmount(accountDO.getFreezeAmount().add(accountDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        accountMapper.update(accountDO);
        inlineService.testInline();
    }

    @Override
    public boolean testPayment(AccountDTO accountDTO) {
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountDO.setBalance(accountDO.getBalance().subtract(accountDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        accountDO.setFreezeAmount(new BigDecimal(0));
        accountMapper.update(accountDO);
        return Boolean.TRUE;
    }


    /**
     * 扣款支付
     *
     * @param accountNestedDTO 参数dto
     * @return true
     */
    @Override
    @Hmily(confirmMethod = "confirmNested", cancelMethod = "cancelNested")
    @Transactional
    public boolean paymentWithNested(AccountNestedDTO accountNestedDTO) {
        final AccountDO accountDO = accountMapper.findByUserId(accountNestedDTO.getUserId());
        accountDO.setBalance(accountDO.getBalance().subtract(accountNestedDTO.getAmount()));
        accountDO.setFreezeAmount(accountDO.getFreezeAmount().add(accountNestedDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        accountMapper.update(accountDO);

        InventoryDTO inventoryDTO = new InventoryDTO();

        inventoryDTO.setCount(accountNestedDTO.getCount());
        inventoryDTO.setProductId(accountNestedDTO.getProductId());
        inventoryService.decrease(inventoryDTO);
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

    @Transactional
    public boolean confirmNested(AccountNestedDTO accountNestedDTO) {
        LOGGER.debug("============dubbo tcc 执行确认付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountNestedDTO.getUserId());
        accountDO.setFreezeAmount(accountDO.getFreezeAmount().subtract(accountNestedDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        accountMapper.confirm(accountDO);
        return Boolean.TRUE;
    }

    @Transactional
    public boolean cancelNested(AccountNestedDTO accountNestedDTO) {
        LOGGER.debug("============ dubbo tcc 执行取消付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountNestedDTO.getUserId());
        accountDO.setBalance(accountDO.getBalance().add(accountNestedDTO.getAmount()));
        accountDO.setFreezeAmount(accountDO.getFreezeAmount().subtract(accountNestedDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        accountMapper.cancel(accountDO);
        return Boolean.TRUE;
    }

    @Transactional
    public boolean confirm(AccountDTO accountDTO) {
        LOGGER.debug("============dubbo tcc 执行确认付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountDO.setFreezeAmount(accountDO.getFreezeAmount().subtract(accountDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        accountMapper.confirm(accountDO);
        return Boolean.TRUE;
    }


    @Transactional
    public boolean cancel(AccountDTO accountDTO) {
        LOGGER.debug("============ dubbo tcc 执行取消付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountDO.setBalance(accountDO.getBalance().add(accountDTO.getAmount()));
        accountDO.setFreezeAmount(accountDO.getFreezeAmount().subtract(accountDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        accountMapper.cancel(accountDO);
        return Boolean.TRUE;
    }
}
