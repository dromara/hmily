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

import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.common.account.dto.AccountNestedDTO;
import org.dromara.hmily.demo.common.account.entity.AccountDO;
import org.dromara.hmily.demo.common.account.mapper.AccountMapper;
import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.dromara.hmily.demo.springcloud.account.client.InventoryClient;
import org.dromara.hmily.demo.springcloud.account.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Account service.
 *
 * @author xiaoyu
 */
@Service("accountService")
public class AccountServiceImpl implements AccountService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountMapper accountMapper;
    
    private final InventoryClient inventoryClient;
    
    /**
     * Instantiates a new Account service.
     *
     * @param accountMapper the account mapper
     */
    @Autowired(required = false)
    public AccountServiceImpl(final AccountMapper accountMapper, final InventoryClient inventoryClient) {
        this.accountMapper = accountMapper;
        this.inventoryClient = inventoryClient;
    }

    @Override
    @HmilyTCC(confirmMethod = "confirm", cancelMethod = "cancel")
    public boolean payment(final AccountDTO accountDTO) {
        LOGGER.info("============执行try付款接口===============");
        accountMapper.update(accountDTO);
        return Boolean.TRUE;
    }
    
    @Override
    public boolean testPayment(AccountDTO accountDTO) {
        accountMapper.testUpdate(accountDTO);
        return Boolean.TRUE;
    }
    
    @Override
    @HmilyTCC(confirmMethod = "confirm", cancelMethod = "cancel")
    public boolean mockWithTryException(AccountDTO accountDTO) {
        throw new HmilyRuntimeException("账户扣减异常！");
    }
    
    @Override
    @HmilyTCC(confirmMethod = "confirm", cancelMethod = "cancel")
    public boolean mockWithTryTimeout(AccountDTO accountDTO) {
        try {
            //模拟延迟 当前线程暂停10秒
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int decrease = accountMapper.update(accountDTO);
        if (decrease != 1) {
            throw new HmilyRuntimeException("账户余额不足");
        }
        return true;
    }
    
    @Override
    @HmilyTCC(confirmMethod = "confirmNested", cancelMethod = "cancelNested")
    public boolean paymentWithNested(AccountNestedDTO nestedDTO) {
        accountMapper.update(buildAccountDTO(nestedDTO));
        inventoryClient.decrease(buildInventoryDTO(nestedDTO));
        return Boolean.TRUE;
    }
    
    @Override
    @HmilyTCC(confirmMethod = "confirmNested", cancelMethod = "cancelNested")
    public boolean paymentWithNestedException(AccountNestedDTO nestedDTO) {
        accountMapper.update(buildAccountDTO(nestedDTO));
        inventoryClient.mockWithTryException(buildInventoryDTO(nestedDTO));
        return Boolean.TRUE;
    }
    
    @Override
    public AccountDO findByUserId(final String userId) {
        return accountMapper.findByUserId(userId);
    }

    /**
     * Confirm boolean.
     *
     * @param accountDTO the account dto
     * @return the boolean
     */
    public boolean confirm(final AccountDTO accountDTO) {
        LOGGER.info("============执行confirm 付款接口===============");
        return accountMapper.confirm(accountDTO) > 0;
    }


    /**
     * Cancel boolean.
     *
     * @param accountDTO the account dto
     * @return the boolean
     */
    public boolean cancel(final AccountDTO accountDTO) {
        LOGGER.info("============执行cancel 付款接口===============");
        return accountMapper.cancel(accountDTO) > 0;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmNested(AccountNestedDTO accountNestedDTO) {
        LOGGER.info("============confirmNested确认付款接口===============");
        return accountMapper.confirm(buildAccountDTO(accountNestedDTO)) > 0;
    }
    
    /**
     * Cancel nested boolean.
     *
     * @param accountNestedDTO the account nested dto
     * @return the boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelNested(AccountNestedDTO accountNestedDTO) {
        LOGGER.info("============cancelNested 执行取消付款接口===============");
        return accountMapper.cancel(buildAccountDTO(accountNestedDTO)) > 0;
    }
    
    private AccountDTO buildAccountDTO(AccountNestedDTO nestedDTO) {
        AccountDTO dto = new AccountDTO();
        dto.setAmount(nestedDTO.getAmount());
        dto.setUserId(nestedDTO.getUserId());
        return dto;
    }
    
    private InventoryDTO buildInventoryDTO(AccountNestedDTO nestedDTO) {
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(nestedDTO.getCount());
        inventoryDTO.setProductId(nestedDTO.getProductId());
        return inventoryDTO;
    }
}
