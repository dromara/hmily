package org.dromara.hmily.demo.grpc.account.service;

import io.grpc.stub.StreamObserver;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.demo.grpc.account.dto.AccountRequest;
import org.dromara.hmily.demo.grpc.account.dto.AccountResponse;
import org.springframework.stereotype.Service;

/**
 * @author lilang
 * @date 2020-09-13 20:33
 **/
@Service
public class InlineAccountServiceImpl extends InlineAccountServiceGrpc.InlineAccountServiceImplBase {

    @Override
    @HmilyTCC(confirmMethod = "confirm", cancelMethod = "cancel")
    public void testInline(AccountRequest request, StreamObserver<AccountResponse> responseObserver) {
        System.out.println("执行inline try......");
        responseObserver.onCompleted();
    }

    /**
     * Confrim.
     */
    public void confirm() {
        System.out.println("执行inline confirm......");
    }

    /**
     * Cancel.
     */
    public void cancel() {
        System.out.println("执行inline cancel......");
    }
}
