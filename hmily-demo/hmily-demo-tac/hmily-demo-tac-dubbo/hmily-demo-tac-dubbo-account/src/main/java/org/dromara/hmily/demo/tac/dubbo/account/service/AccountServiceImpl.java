/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.demo.tac.dubbo.account.service;

import org.dromara.hmily.annotation.HmilyTAC;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.demo.common.account.api.AccountService;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.common.account.dto.AccountNestedDTO;
import org.dromara.hmily.demo.common.account.entity.AccountDO;
import org.dromara.hmily.demo.common.account.mapper.AccountMapper;
import org.dromara.hmily.demo.common.inventory.api.InventoryService;
import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
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

    private final InventoryService inventoryService;

    /**
     * Instantiates a new Account service.
     *
     * @param accountMapper the account mapper
     */
    @Autowired(required = false)
    public AccountServiceImpl(final AccountMapper accountMapper,
                              final InventoryService inventoryService) {
        this.accountMapper = accountMapper;
        this.inventoryService = inventoryService;
    }

    @Override
    @HmilyTAC
    public boolean payment(final AccountDTO accountDTO) {
        int count = accountMapper.update(accountDTO);
        if (count > 0) {
            return true;
        } else {
            throw new HmilyRuntimeException("账户扣减异常！");
        }
    }
    
    @Override
    @HmilyTAC
    public boolean mockTryPaymentException(final AccountDTO accountDTO) {
        throw new HmilyRuntimeException("账户扣减异常！");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @HmilyTAC
    public boolean mockTryPaymentTimeout(final AccountDTO accountDTO) {
        try {
            //模拟延迟 当前线程暂停10秒
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final int decrease = accountMapper.update(accountDTO);
        if (decrease != 1) {
            throw new HmilyRuntimeException("库存不足");
        }
        return true;
    }
    
    @Override
    public boolean testPayment(final AccountDTO accountDTO) {
        accountMapper.testUpdate(accountDTO);
        return Boolean.TRUE;
    }

    @Override
    @HmilyTAC
    @Transactional(rollbackFor = Exception.class)
    public boolean paymentWithNested(final AccountNestedDTO accountNestedDTO) {
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
    @HmilyTAC
    @Transactional(rollbackFor = Exception.class)
    public boolean paymentWithNestedException(final AccountNestedDTO accountNestedDTO) {
        AccountDTO dto = new AccountDTO();
        dto.setAmount(accountNestedDTO.getAmount());
        dto.setUserId(accountNestedDTO.getUserId());
        accountMapper.update(dto);
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(accountNestedDTO.getCount());
        inventoryDTO.setProductId(accountNestedDTO.getProductId());
        inventoryService.decrease(inventoryDTO);
        //下面这个且套服务异常
        inventoryService.mockWithTryException(inventoryDTO);
        return Boolean.TRUE;
    }

    @HmilyTAC
    @Override
    public AccountDO findByUserId(final String userId) {
        return accountMapper.findByUserId(userId);
    }
}
