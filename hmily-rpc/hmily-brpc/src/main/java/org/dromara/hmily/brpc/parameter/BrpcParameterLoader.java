package org.dromara.hmily.brpc.parameter;

import com.baidu.brpc.RpcContext;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.core.mediator.RpcParameterLoader;
import org.dromara.hmily.spi.HmilySPI;

import java.util.Map;
import java.util.Optional;

/**
 * Create by liu·yu
 * Date is 2020-10-08
 * Description is :
 */
@HmilySPI(value = "brpc")
public class BrpcParameterLoader implements RpcParameterLoader {
    @Override
    public HmilyTransactionContext load() {
        return Optional.ofNullable(RpcMediator.getInstance().acquire(k -> {
            Map<String, Object> attachment = RpcContext.getContext()
                                                       .getRequestKvAttachment();
            if (attachment != null) {
                return String.valueOf(attachment.get(k));
            }
            return null;
        })).orElse(HmilyContextHolder.get());
    }
}
