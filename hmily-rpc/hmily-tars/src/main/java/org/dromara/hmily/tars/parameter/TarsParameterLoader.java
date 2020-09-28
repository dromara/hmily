package org.dromara.hmily.tars.parameter;

import com.qq.tars.server.core.ContextManager;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.core.mediator.RpcParameterLoader;
import org.dromara.hmily.spi.HmilySPI;

import java.util.Optional;

/**
 * The type tars parameter loader.
 *
 * @author tydhot
 */
@HmilySPI(value = "tars")
public class TarsParameterLoader implements RpcParameterLoader {
    @Override
    public HmilyTransactionContext load() {
        if (ContextManager.getContext() != null) {
            return Optional.ofNullable(RpcMediator.getInstance().acquire(ContextManager.getContext()::getAttribute)).orElse(HmilyContextHolder.get());
        }

        return HmilyContextHolder.get();
    }
}
