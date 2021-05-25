package org.dromara.hmily.demo.grpc.account.service;

import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.common.account.entity.AccountDO;
import org.dromara.hmily.demo.common.account.mapper.AccountMapper;
import org.dromara.hmily.demo.grpc.account.AccountServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tydhot
 */
@Component
public class AccountServiceBeanImpl implements AccountServiceBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceBeanImpl.class);

    private final AccountMapper accountMapper;

    @Autowired
    AccountServiceBeanImpl accountServiceBeanImpl;

    /**
     * The Confrim count.
     */
    private static AtomicInteger confrimCount = new AtomicInteger(0);

    @Autowired(required = false)
    public AccountServiceBeanImpl(final AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

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

    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(AccountDTO accountDTO) {
        LOGGER.info("============ tars tcc 执行取消付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountMapper.cancel(accountDTO);
        return Boolean.TRUE;
    }

}
