package org.dromara.hmily.demo.grpc.order.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.dromara.hmily.demo.grpc.account.service.AccountRequest;
import org.dromara.hmily.demo.grpc.account.service.AccountResponse;
import org.dromara.hmily.demo.grpc.account.service.AccountServiceGrpc;
import org.dromara.hmily.grpc.client.GrpcHmilyClient;
import org.dromara.hmily.grpc.filter.GrpcHmilyTransactionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author tydhot
 */
@Component
public class AccountClient {

    private AccountServiceGrpc.AccountServiceBlockingStub accountServiceBlockingStub;
    
    @Autowired
    GrpcHmilyClient grpcHmilyClient;

    @PostConstruct
    private void init() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 28074)
                .intercept(new GrpcHmilyTransactionFilter()).usePlaintext().build();
        accountServiceBlockingStub = AccountServiceGrpc.newBlockingStub(managedChannel);
    }

    public boolean payment(String userId, String amount) {
        AccountRequest request = AccountRequest.newBuilder().setUserId(userId).setAmount(amount).build();

        AccountResponse response = grpcHmilyClient.syncInvoke(accountServiceBlockingStub, "payment", request, AccountResponse.class);

        return response.getResult();
    }

}
