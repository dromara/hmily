package org.dromara.hmily.demo.grpc.account.service;

import io.grpc.stub.StreamObserver;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.grpc.filter.GrpcHmilyServerFilter;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * @author tydhot
 */
@GRpcService(interceptors = {GrpcHmilyServerFilter.class})
public class AccountServiceImpl extends AccountServiceGrpc.AccountServiceImplBase {
    
    @Autowired
    AccountServiceBeanImpl accountServiceBeanImpl;

    @Override
    public void payment(AccountRequest accountRequest, StreamObserver<AccountResponse> responseObserver) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(new BigDecimal(accountRequest.getAmount()));
        accountDTO.setUserId(accountRequest.getUserId());
        AccountResponse response = AccountResponse.newBuilder().setResult(accountServiceBeanImpl.payment(accountDTO)).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
