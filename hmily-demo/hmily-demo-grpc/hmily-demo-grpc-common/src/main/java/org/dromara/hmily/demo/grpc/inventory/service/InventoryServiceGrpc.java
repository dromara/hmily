package org.dromara.hmily.demo.grpc.inventory.service;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.0.0)",
    comments = "Source: inventory_service.proto")
public class InventoryServiceGrpc {

  private InventoryServiceGrpc() {}

  public static final String SERVICE_NAME = "org.dromara.hmily.demo.grpc.inventory.servic.InventoryServiceGrpc$InventoryServiceBlockingStub";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.dromara.hmily.demo.grpc.inventory.service.InventoryRequest,
      org.dromara.hmily.demo.grpc.inventory.service.InventoryResponse> METHOD_DECREASE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "org.dromara.hmily.demo.grpc.inventory.servic.InventoryServiceGrpc$InventoryServiceBlockingStub", "decrease"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.dromara.hmily.demo.grpc.inventory.service.InventoryRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.dromara.hmily.demo.grpc.inventory.service.InventoryResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static InventoryServiceStub newStub(io.grpc.Channel channel) {
    return new InventoryServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static InventoryServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new InventoryServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static InventoryServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new InventoryServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class InventoryServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void decrease(org.dromara.hmily.demo.grpc.inventory.service.InventoryRequest request,
        io.grpc.stub.StreamObserver<org.dromara.hmily.demo.grpc.inventory.service.InventoryResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DECREASE, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_DECREASE,
            asyncUnaryCall(
              new MethodHandlers<
                org.dromara.hmily.demo.grpc.inventory.service.InventoryRequest,
                org.dromara.hmily.demo.grpc.inventory.service.InventoryResponse>(
                  this, METHODID_DECREASE)))
          .build();
    }
  }

  /**
   */
  public static final class InventoryServiceStub extends io.grpc.stub.AbstractStub<InventoryServiceStub> {
    private InventoryServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private InventoryServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InventoryServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new InventoryServiceStub(channel, callOptions);
    }

    /**
     */
    public void decrease(org.dromara.hmily.demo.grpc.inventory.service.InventoryRequest request,
        io.grpc.stub.StreamObserver<org.dromara.hmily.demo.grpc.inventory.service.InventoryResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DECREASE, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class InventoryServiceBlockingStub extends io.grpc.stub.AbstractStub<InventoryServiceBlockingStub> {
    private InventoryServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private InventoryServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InventoryServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new InventoryServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.dromara.hmily.demo.grpc.inventory.service.InventoryResponse decrease(org.dromara.hmily.demo.grpc.inventory.service.InventoryRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DECREASE, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class InventoryServiceFutureStub extends io.grpc.stub.AbstractStub<InventoryServiceFutureStub> {
    private InventoryServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private InventoryServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InventoryServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new InventoryServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.dromara.hmily.demo.grpc.inventory.service.InventoryResponse> decrease(
        org.dromara.hmily.demo.grpc.inventory.service.InventoryRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DECREASE, getCallOptions()), request);
    }
  }

  private static final int METHODID_DECREASE = 0;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final InventoryServiceImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(InventoryServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_DECREASE:
          serviceImpl.decrease((org.dromara.hmily.demo.grpc.inventory.service.InventoryRequest) request,
              (io.grpc.stub.StreamObserver<org.dromara.hmily.demo.grpc.inventory.service.InventoryResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    return new io.grpc.ServiceDescriptor(SERVICE_NAME,
        METHOD_DECREASE);
  }

}
