package org.dromara.hmily.grpc.filter;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ForwardingServerCall;
import org.dromara.hmily.grpc.parameter.GrpcHmilyContext;

/**
 * Grpc server filter.
 *
 * @author tydhot
 */
public class GrpcHmilyServerFilter implements ServerInterceptor {

    @Override
    public <R, P> ServerCall.Listener<R> interceptCall(final ServerCall<R, P> serverCall,
                                                       final Metadata metadata, final ServerCallHandler<R, P> serverCallHandler) {
        GrpcHmilyContext.getHmilyContext().set(metadata.get(GrpcHmilyContext.HMILY_META_DATA));
        return serverCallHandler.startCall(new ForwardingServerCall.SimpleForwardingServerCall<R, P>(serverCall) {
            @Override
            public void sendMessage(final P message) {
                GrpcHmilyContext.getHmilyContext().remove();
                super.sendMessage(message);
            }
        }, metadata);
    }
}
