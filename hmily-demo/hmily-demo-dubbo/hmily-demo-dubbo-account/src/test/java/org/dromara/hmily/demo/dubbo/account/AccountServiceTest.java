package org.dromara.hmily.demo.dubbo.account;

import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.demo.common.account.api.AccountService;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.common.account.entity.AccountDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceTest.class);

    private static String testUserId = "10000";

    @Autowired
    private AccountService accountService;


    @Test
    public void paymentTest() {
        logAccountDO();

        AccountDTO accountDTO = createAccountDTO();
        try {
            accountService.payment(accountDTO);
        }catch (HmilyRuntimeException e) {
            LOGGER.info(e.getMessage());
        }finally {
            logAccountDO();
        }
    }


    @Test
    public void mockTryPaymentExceptionTest() {
        logAccountDO();

        AccountDTO accountDTO = createAccountDTO();
        try {
            accountService.mockTryPaymentException(accountDTO);
        } catch (HmilyRuntimeException e) {
            LOGGER.info(e.getMessage());
        } finally {
            logAccountDO();
        }
    }


    @Test
    public void mockTryPaymentTimeoutTest() {
        logAccountDO();

        AccountDTO accountDTO = createAccountDTO();
        try {
            accountService.mockTryPaymentTimeout(accountDTO);
        }catch (HmilyRuntimeException e) {
            LOGGER.info(e.getMessage());
        }finally {
            logAccountDO();
        }
    }


    private AccountDTO createAccountDTO() {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserId(testUserId);
        accountDTO.setAmount(new BigDecimal("10000010"));
        return accountDTO;
    }

    private void logAccountDO(){
        AccountDO accountDO = accountService.findByUserId(testUserId);
        LOGGER.info("当前金额：" + accountDO.getBalance() + ", 冻结金额：" + accountDO.getFreezeAmount());
    }
}
