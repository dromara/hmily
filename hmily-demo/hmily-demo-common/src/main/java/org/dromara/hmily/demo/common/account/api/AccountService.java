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

package org.dromara.hmily.demo.common.account.api;

import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.common.account.dto.AccountNestedDTO;
import org.dromara.hmily.demo.common.account.entity.AccountDO;

/**
 * The interface Account service.
 *
 * @author xiaoyu
 */
public interface AccountService {
    
    /**
     * 扣款支付
     *
     * @param accountDTO 参数dto
     */
    @Hmily
    boolean payment(AccountDTO accountDTO);
    
    /**
     * Mock try payment exception.
     *
     * @param accountDTO the account dto
     */
    @Hmily
    boolean mockTryPaymentException(AccountDTO accountDTO);
    
    /**
     * Mock try payment timeout.
     *
     * @param accountDTO the account dto
     */
    @Hmily
    boolean mockTryPaymentTimeout(AccountDTO accountDTO);
    
    /**
     * Payment tac boolean.
     *
     * @param accountDTO the account dto
     * @return the boolean
     */
    @Hmily
    boolean paymentTAC(AccountDTO accountDTO);
    
    /**
     * Test payment boolean.
     *
     * @param accountDTO the account dto
     * @return the boolean
     */
    boolean testPayment(AccountDTO accountDTO);
    
    /**
     * 扣款支付
     *
     * @param accountNestedDTO 参数dto
     * @return true boolean
     */
    @Hmily
    boolean paymentWithNested(AccountNestedDTO accountNestedDTO);
    
    /**
     * Payment with nested exception boolean.
     *
     * @param accountNestedDTO the account nested dto
     * @return the boolean
     */
    @Hmily
    boolean paymentWithNestedException(AccountNestedDTO accountNestedDTO);
    
    /**
     * 获取用户账户信息
     *
     * @param userId 用户id
     * @return AccountDO account do
     */
    AccountDO findByUserId(String userId);
}
