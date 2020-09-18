package org.dromara.hmily.demo.tars.account.servant.accountapp.impl;

import com.qq.tars.spring.annotation.TarsServant;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.tars.account.servant.accountapp.AccountServant;
import org.dromara.hmily.demo.tars.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * @Author tydhot
 */
@TarsServant("AccountObj")
public class AccountServantImpl implements AccountServant {

    private final AccountService accountService;

    @Autowired(required = false)
    public AccountServantImpl(final AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void payment(String userId, double amount) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserId(userId);
        accountDTO.setAmount(new BigDecimal(amount));
        accountService.payment(accountDTO);
    }
}
