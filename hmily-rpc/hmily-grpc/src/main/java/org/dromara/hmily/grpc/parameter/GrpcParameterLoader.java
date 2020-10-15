package org.dromara.hmily.grpc.parameter;

import java.util.Optional;

import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.core.mediator.RpcParameterLoader;
import org.dromara.hmily.spi.HmilySPI;

/**
 * grpc paramter loader.
 *
 * @author tydhot
 */
@HmilySPI(value = "grpc")
public class GrpcParameterLoader implements RpcParameterLoader {

    @Override
    public HmilyTransactionContext load() {
        return Optional.ofNullable(RpcMediator.getInstance().acquire(key -> GrpcHmilyContext.getHmilyContext().get()))
            .orElse(HmilyContextHolder.get());
    }

}
