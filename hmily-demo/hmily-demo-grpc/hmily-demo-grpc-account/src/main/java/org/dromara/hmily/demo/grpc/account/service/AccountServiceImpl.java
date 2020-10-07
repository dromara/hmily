package org.dromara.hmily.demo.grpc.account.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.dromara.hmily.annotation.HmilyTAC;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.demo.common.account.api.InlineService;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.common.account.entity.AccountDO;
import org.dromara.hmily.demo.common.account.mapper.AccountMapper;
import org.dromara.hmily.demo.common.inventory.api.InventoryService;
import org.dromara.hmily.demo.common.inventory.dto.InventoryDTO;
import org.dromara.hmily.demo.grpc.account.dto.AccountNestedRequest;
import org.dromara.hmily.demo.grpc.account.dto.AccountRequest;
import org.dromara.hmily.demo.grpc.account.dto.AccountResponse;
import org.dromara.hmily.demo.grpc.inventory.dto.InventoryRequest;
import org.dromara.hmily.demo.grpc.inventory.service.InventoryServiceGrpc;
import org.dromara.hmily.grpc.rpc.interceptor.GrpcHmilyTransactionServerInterceptor;
import org.dromara.hmily.grpc.rpc.wrapper.HmilyGrpcStubWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lilang
 * @date 2020-09-13 19:21
 **/
@GrpcService(interceptors = {GrpcHmilyTransactionServerInterceptor.class})
public class AccountServiceImpl extends AccountServiceGrpc.AccountServiceImplBase {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

    /**
     * The Trycount.
     */
    private static AtomicInteger trycount = new AtomicInteger(0);

    /**
     * The Confrim count.
     */
    private static AtomicInteger confrimCount = new AtomicInteger(0);

    @Autowired
    private AccountMapper accountMapper;

    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceBlockingStub;

    public AccountServiceImpl(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8873).build();

        inventoryServiceBlockingStub = HmilyGrpcStubWrapper.wrap(InventoryServiceGrpc.newBlockingStub(channel));
    }


    @Override
    @HmilyTCC(confirmMethod = "confirm", cancelMethod = "cancel")
    public void payment(AccountRequest accountRequest, StreamObserver<AccountResponse> responseObserver) {
        AccountDTO accountDTO = mappingFromRequest(accountRequest);
        accountMapper.update(accountDTO);
        responseObserver.onNext(AccountResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    private AccountDTO mappingFromRequest(AccountRequest accountRequest) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserId(accountRequest.getUserId());
        accountDTO.setAmount(new BigDecimal(accountRequest.getAmount()));
        return accountDTO;
    }

    @Override
    @HmilyTCC(confirmMethod = "confirm", cancelMethod = "cancel")
    public void mockTryPaymentException(AccountRequest accountRequest, StreamObserver<AccountResponse> responseObserver) {
        responseObserver.onError(new HmilyRuntimeException("账户扣减异常！"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @HmilyTCC(confirmMethod = "confirm", cancelMethod = "cancel")
    public void mockTryPaymentTimeout(AccountRequest accountRequest, StreamObserver<AccountResponse> responseObserver) {
        try {
            //模拟延迟 当前线程暂停10秒
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final int decrease = accountMapper.update(mappingFromRequest(accountRequest));
        if (decrease != 1) {
            responseObserver.onError(new HmilyRuntimeException("库存不足"));
        }
    }

    @Override
    @HmilyTAC
    public void paymentTAC(AccountRequest accountRequest, StreamObserver<AccountResponse> responseObserver) {
        accountMapper.update(mappingFromRequest(accountRequest));
        responseObserver.onNext(AccountResponse.newBuilder().setResult(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void testPayment(AccountRequest accountRequest, StreamObserver<AccountResponse> responseObserver) {
        accountMapper.testUpdate(mappingFromRequest(accountRequest));
        responseObserver.onNext(AccountResponse.newBuilder().setResult(true).build());
        responseObserver.onCompleted();
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmNested", cancelMethod = "cancelNested")
    @Transactional(rollbackFor = Exception.class)
    public void paymentWithNested(AccountNestedRequest accountNestedRequest, StreamObserver<AccountResponse> responseObserver) {
        AccountDTO dto = mappingFromRequest(accountNestedRequest);
        accountMapper.update(dto);
        InventoryRequest inventoryRequest = InventoryRequest.newBuilder()
                .setCount(accountNestedRequest.getCount())
                .setProductId(accountNestedRequest.getProductId())
                .build();

        inventoryServiceBlockingStub.decrease(inventoryRequest);
        responseObserver.onNext(AccountResponse.newBuilder().setResult(true).build());
        responseObserver.onCompleted();
    }

    private AccountDTO mappingFromRequest(AccountNestedRequest accountNestedRequest) {
        AccountDTO dto = new AccountDTO();
        dto.setAmount(new BigDecimal(accountNestedRequest.getAmount()));
        dto.setUserId(accountNestedRequest.getUserId());
        return dto;
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmNested", cancelMethod = "cancelNested")
    @Transactional(rollbackFor = Exception.class)
    public void paymentWithNestedException(AccountNestedRequest accountNestedRequest, StreamObserver<AccountResponse> responseObserver) {
        accountMapper.update(mappingFromRequest(accountNestedRequest));

        InventoryRequest inventoryRequest = InventoryRequest.newBuilder()
                .setCount(accountNestedRequest.getCount())
                .setProductId(accountNestedRequest.getProductId())
                .build();
        inventoryServiceBlockingStub.decrease(inventoryRequest);
        //下面这个且套服务异常
        inventoryServiceBlockingStub.mockWithTryException(inventoryRequest);
        responseObserver.onNext(AccountResponse.newBuilder().setResult(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void findByUserId(AccountRequest accountRequest, StreamObserver<org.dromara.hmily.demo.grpc.account.dto.AccountDO> responseObserver) {
        AccountDO accountDO = accountMapper.findByUserId(accountRequest.getUserId());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        org.dromara.hmily.demo.grpc.account.dto.AccountDO accountDOResponse = org.dromara.hmily.demo.grpc.account.dto.AccountDO.newBuilder()
                .setId(accountDO.getId())
                .setBalance(String.valueOf(accountDO.getBalance()))
                .setCreateTime(df.format(accountDO.getCreateTime()))
                .setUpdateTime(df.format(accountDO.getBalance()))
                .setFreezeAmount(String.valueOf(accountDO.getFreezeAmount()))
                .setUserId(accountDO.getUserId())
                .build();
        responseObserver.onNext(accountDOResponse);
        responseObserver.onCompleted();
    }

    /**
     * Confirm nested boolean.
     *
     * @param accountNestedRequest the account nested dto
     * @return the boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmNested(AccountNestedRequest accountNestedRequest) {
        LOGGER.debug("============ grpc tcc 执行确认付款接口===============");
        org.dromara.hmily.demo.common.account.dto.AccountDTO accountDTO = new org.dromara.hmily.demo.common.account.dto.AccountDTO();
        accountDTO.setUserId(accountNestedRequest.getUserId());
        accountDTO.setAmount(new BigDecimal(accountNestedRequest.getAmount()));
        accountMapper.confirm(accountDTO);
        return Boolean.TRUE;
    }

    /**
     * Cancel nested boolean.
     *
     * @param accountNestedRequest the account nested dto
     * @return the boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelNested(AccountNestedRequest accountNestedRequest) {
        LOGGER.debug("============ grpc tcc 执行取消付款接口===============");
        org.dromara.hmily.demo.common.account.dto.AccountDTO accountDTO = new org.dromara.hmily.demo.common.account.dto.AccountDTO();
        accountDTO.setUserId(accountNestedRequest.getUserId());
        accountDTO.setAmount(new BigDecimal(accountNestedRequest.getAmount()));
        accountMapper.cancel(accountDTO);
        return Boolean.TRUE;
    }

    /**
     * Confirm boolean.
     *
     * @param accountRequest the account dto
     * @return the boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean confirm(AccountRequest accountRequest) {
        LOGGER.info("============grpc tcc 执行确认付款接口===============");
        accountMapper.confirm(mappingFromRequest(accountRequest));
        final int i = confrimCount.incrementAndGet();
        LOGGER.info("调用了account confirm " + i + " 次");
        return Boolean.TRUE;
    }

    /**
     * Cancel boolean.
     *
     * @param accountRequest the account dto
     * @return the boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(AccountRequest accountRequest) {
        LOGGER.info("============ grpc tcc 执行取消付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountRequest.getUserId());
        accountMapper.cancel(mappingFromRequest(accountRequest));
        return Boolean.TRUE;
    }
}
