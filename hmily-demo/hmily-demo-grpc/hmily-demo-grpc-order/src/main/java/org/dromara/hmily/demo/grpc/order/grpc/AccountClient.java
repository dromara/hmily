package org.dromara.hmily.demo.grpc.order.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.dromara.hmily.core.holder.SingletonHolder;
import org.dromara.hmily.demo.grpc.account.service.AccountRequest;
import org.dromara.hmily.demo.grpc.account.service.AccountResponse;
import org.dromara.hmily.demo.grpc.account.service.AccountServiceGrpc;
import org.dromara.hmily.grpc.filter.GrpcHmilyTransactionFilter;
import org.dromara.hmily.grpc.parameter.GrpcHmilyContext;
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
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 28074)
                .intercept(new GrpcHmilyTransactionFilter()).usePlaintext().build();
        accountServiceBlockingStub = AccountServiceGrpc.newBlockingStub(managedChannel);
        SingletonHolder.INST.register(AccountServiceGrpc.AccountServiceBlockingStub.class, accountServiceBlockingStub);
    }

    public boolean payment(String userId, String amount) {
        AccountRequest request = AccountRequest.newBuilder().setUserId(userId).setAmount(amount).build();
        GrpcHmilyContext.getHmilyParam().set(request);

        AccountResponse response = accountServiceBlockingStub.payment(request);

        return response.getResult();
    }

}
