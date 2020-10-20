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

package org.dromara.hmily.demo.tars.account.servant.accountapp.impl;

import com.qq.tars.spring.annotation.TarsServant;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.tars.account.servant.accountapp.AccountServant;
import org.dromara.hmily.demo.tars.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * @author tydhot
 */
@TarsServant("AccountObj")
public class AccountServantImpl implements AccountServant {

    private final AccountService accountService;

    @Autowired(required = false)
    public AccountServantImpl(final AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public boolean payment(String userId, double amount) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserId(userId);
        accountDTO.setAmount(new BigDecimal(amount));
        return accountService.payment(accountDTO);
    }
}
