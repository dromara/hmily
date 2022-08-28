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

package org.dromara.hmily.demo.springcloud.account.controller;

import org.dromara.hmily.demo.common.account.api.AccountService;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.common.account.dto.AccountNestedDTO;
import org.dromara.hmily.demo.springcloud.account.service.impl.AccountServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * AccountController.
 * @author xiaoyu
 */
@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountServiceImpl accountService) {
        this.accountService = accountService;
    }

    @RequestMapping("/payment")
    public Boolean payment(@RequestBody AccountDTO accountDO) {
        return accountService.payment(accountDO);
    }

    @RequestMapping("/testPayment")
    public Boolean testPayment(@RequestBody AccountDTO accountDO) {
        return accountService.testPayment(accountDO);
    }


    @RequestMapping("/paymentWithNested")
    public Boolean paymentWithNested(@RequestBody AccountNestedDTO nestedDTO) {
        return accountService.paymentWithNested(nestedDTO);
    }

    @RequestMapping("/paymentWithNestedException")
    public Boolean paymentWithNestedException(@RequestBody AccountNestedDTO nestedDTO) {
        return accountService.paymentWithNestedException(nestedDTO);
    }

//    @RequestMapping("/paymentWithNestedTimeout")
//    public Boolean paymentWithNestedTimeout(@RequestBody AccountNestedDTO nestedDTO) {
//        return accountService.paymentWithNestedTimeout(nestedDTO);
//    }

    @RequestMapping("/findByUserId")
    public BigDecimal findByUserId(@RequestParam("userId") String userId) {
        return accountService.findByUserId(userId).getBalance();
    }
}
