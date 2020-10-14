package org.dromara.hmily.demo.grpc.account.service;

import io.grpc.stub.StreamObserver;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.common.account.entity.AccountDO;
import org.dromara.hmily.demo.common.account.mapper.AccountMapper;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tydhot
 */
@GRpcService
public class AccountServiceImpl extends AccountServiceGrpc.AccountServiceImplBase {

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
    public void payment(AccountRequest accountRequest, StreamObserver<AccountResponse> responseObserver) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(new BigDecimal(accountRequest.getAmount()));
        accountDTO.setUserId(accountRequest.getUserId());
        AccountResponse response = AccountResponse.newBuilder().setResult(accountMapper.update(accountDTO) > 0).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirm(AccountRequest accountRequest) {
        LOGGER.info("============tars tcc 执行确认付款接口===============");
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(new BigDecimal(accountRequest.getAmount()));
        accountDTO.setUserId(accountRequest.getUserId());
        accountMapper.confirm(accountDTO);
        final int i = confrimCount.incrementAndGet();
        LOGGER.info("调用了account confirm " + i + " 次");
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(AccountRequest accountRequest) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(new BigDecimal(accountRequest.getAmount()));
        accountDTO.setUserId(accountRequest.getUserId());
        LOGGER.info("============ tars tcc 执行取消付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountMapper.cancel(accountDTO);
        return Boolean.TRUE;
    }

}
