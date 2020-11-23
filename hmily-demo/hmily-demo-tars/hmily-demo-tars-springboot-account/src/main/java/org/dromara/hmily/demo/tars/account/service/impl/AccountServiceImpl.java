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

package org.dromara.hmily.demo.tars.account.service.impl;

import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.common.account.entity.AccountDO;
import org.dromara.hmily.demo.common.account.mapper.AccountMapper;
import org.dromara.hmily.demo.tars.account.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tydhot
 */
@Service("accountService")
public class AccountServiceImpl implements AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);
    
    private final AccountMapper accountMapper;

    /**
     * The Confrim count.
     */
    private static AtomicInteger confrimCount = new AtomicInteger(0);

    @Autowired(required = false)
    public AccountServiceImpl(final AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    @HmilyTCC(confirmMethod = "confirm", cancelMethod = "cancel")
    public boolean payment(AccountDTO accountDTO) {
        return accountMapper.update(accountDTO) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirm(AccountDTO accountDTO) {
        LOGGER.info("============tars tcc 执行确认付款接口===============");
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
        LOGGER.info("============ tars tcc 执行取消付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountMapper.cancel(accountDTO);
        return Boolean.TRUE;
    }
}
