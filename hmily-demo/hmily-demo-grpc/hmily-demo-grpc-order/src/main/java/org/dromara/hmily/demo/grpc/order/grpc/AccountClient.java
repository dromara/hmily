package org.dromara.hmily.demo.grpc.order.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.dromara.hmily.demo.grpc.account.service.AccountRequest;
import org.dromara.hmily.demo.grpc.account.service.AccountResponse;
import org.dromara.hmily.demo.grpc.account.service.AccountServiceGrpc;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author tydhot
 */
@Component
public class AccountClient {

    private AccountServiceGrpc.AccountServiceBlockingStub accountServiceBlockingStub;

    @PostConstruct
    private void init() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 28074).usePlaintext().build();
        accountServiceBlockingStub = AccountServiceGrpc.newBlockingStub(managedChannel);
    }

    public boolean payment(String userId, String amount) {
        AccountRequest request = AccountRequest.newBuilder().setUserId(userId).setAmount(amount).build();

        AccountResponse response = accountServiceBlockingStub.payment(request);

        return response.getResult();
    }

}
