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

package org.dromara.hmily.demo.springcloud.account.service;

import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.common.account.dto.AccountNestedDTO;
import org.dromara.hmily.demo.common.account.entity.AccountDO;

/**
 * AccountService.
 *
 * @author xiaoyu
 */
public interface AccountService {
    
    /**
     * 扣款支付.
     *
     * @param accountDTO 参数dto
     * @return true boolean
     */
    boolean payment(AccountDTO accountDTO);
    
    /**
     * Test payment boolean.
     *
     * @param accountDTO the account dto
     * @return the boolean
     */
    boolean testPayment(AccountDTO accountDTO);
    
    /**
     * Mock with try exception boolean.
     *
     * @param accountDTO the account dto
     * @return the boolean
     */
    boolean mockWithTryException(AccountDTO accountDTO);
    
    /**
     * Mock with try timeout boolean.
     *
     * @param accountDTO the account dto
     * @return the boolean
     */
    boolean mockWithTryTimeout(AccountDTO accountDTO);
    
    /**
     * Payment with nested boolean.
     *
     * @param nestedDTO the nested dto
     * @return the boolean
     */
    boolean paymentWithNested(AccountNestedDTO nestedDTO);
    
    /**
     * Payment with nested exception boolean.
     *
     * @param nestedDTO the nested dto
     * @return the boolean
     */
    boolean paymentWithNestedException(AccountNestedDTO nestedDTO);
    
    /**
     * 获取用户账户信息.
     *
     * @param userId 用户id
     * @return AccountDO account do
     */
    AccountDO findByUserId(String userId);
}
