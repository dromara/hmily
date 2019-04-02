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

import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * The Trycount.
     */
    private static AtomicInteger trycount = new AtomicInteger(0);

    /**
     * The Confrim count.
     */
    private static AtomicInteger confrimCount = new AtomicInteger(0);

    private final AccountMapper accountMapper;

    private final InventoryService inventoryService;

    private final InlineService inlineService;

    /**
     * Instantiates a new Account service.
     *
     * @param accountMapper the account mapper
     */
    @Autowired(required = false)
    public AccountServiceImpl(final AccountMapper accountMapper,
                              final InventoryService inventoryService,
                              final InlineService inlineService) {
        this.accountMapper = accountMapper;
        this.inventoryService = inventoryService;
        this.inlineService = inlineService;
    }

    @Override
    @Hmily(confirmMethod = "confirm", cancelMethod = "cancel")
    public void payment(AccountDTO accountDTO) {
        accountMapper.update(accountDTO);
        /*final int i = trycount.incrementAndGet();
        System.out.println("调用了account try " + i + " 次");*/

        //内嵌 本地的service
        //inlineService.testInline();

        //内嵌 远端的rpc服务  注意如果是内嵌的调用rpc，那么在这次事务里面，不能再调用该RPC
        // inventoryService.testInLine();
    }

    @Override
    public boolean testPayment(AccountDTO accountDTO) {
        accountMapper.update(accountDTO);
        return Boolean.TRUE;
    }

    @Override
    @Hmily(confirmMethod = "confirmNested", cancelMethod = "cancelNested")
    @Transactional(rollbackFor = Exception.class)
    public boolean paymentWithNested(AccountNestedDTO accountNestedDTO) {
        AccountDTO dto = new AccountDTO();
        dto.setAmount(accountNestedDTO.getAmount());
        dto.setUserId(accountNestedDTO.getUserId());
        accountMapper.update(dto);

        InventoryDTO inventoryDTO = new InventoryDTO();

        inventoryDTO.setCount(accountNestedDTO.getCount());
        inventoryDTO.setProductId(accountNestedDTO.getProductId());
        inventoryService.decrease(inventoryDTO);
        return Boolean.TRUE;
    }

    @Override
    public AccountDO findByUserId(String userId) {
        return accountMapper.findByUserId(userId);
    }

    /**
     * Confirm nested boolean.
     *
     * @param accountNestedDTO the account nested dto
     * @return the boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmNested(AccountNestedDTO accountNestedDTO) {
        LOGGER.debug("============dubbo tcc 执行确认付款接口===============");
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserId(accountNestedDTO.getUserId());
        accountDTO.setAmount(accountNestedDTO.getAmount());
        accountMapper.confirm(accountDTO);
        return Boolean.TRUE;
    }

    /**
     * Cancel nested boolean.
     *
     * @param accountNestedDTO the account nested dto
     * @return the boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelNested(AccountNestedDTO accountNestedDTO) {
        LOGGER.debug("============ dubbo tcc 执行取消付款接口===============");
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserId(accountNestedDTO.getUserId());
        accountDTO.setAmount(accountNestedDTO.getAmount());
        accountMapper.cancel(accountDTO);
        return Boolean.TRUE;
    }

    /**
     * Confirm boolean.
     *
     * @param accountDTO the account dto
     * @return the boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean confirm(AccountDTO accountDTO) {
        LOGGER.info("============dubbo tcc 执行确认付款接口===============");
        accountMapper.confirm(accountDTO);
        final int i = confrimCount.incrementAndGet();
        LOGGER.info("调用了account confirm " + i + " 次");
        return Boolean.TRUE;
    }


    /**
     * Cancel boolean.
     *
     * @param accountDTO the account dto
     * @return the boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(AccountDTO accountDTO) {
        LOGGER.info("============ dubbo tcc 执行取消付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountMapper.cancel(accountDTO);
        return Boolean.TRUE;
    }
}
