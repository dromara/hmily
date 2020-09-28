package org.dromara.hmily.tars.loadbalance;

import com.qq.tars.rpc.common.InvokeContext;
import com.qq.tars.rpc.common.Invoker;
import com.qq.tars.rpc.common.LoadBalance;
import com.qq.tars.rpc.common.exc.NoInvokerException;

import java.util.Collection;

/**
 * add hmily load balance.
 *
 * @author tydhot
 */
public class HmilyLoadBalance<T> implements LoadBalance<T> {

    private final HmilyRoundRobinLoadBalance<T> hmilyRoundRobinLoadBalance;

    public HmilyLoadBalance(final HmilyRoundRobinLoadBalance hmilyRoundRobinLoadBalance) {
        this.hmilyRoundRobinLoadBalance = hmilyRoundRobinLoadBalance;
    }

    @Override
    public Invoker<T> select(final InvokeContext invocation) throws NoInvokerException {
        return hmilyRoundRobinLoadBalance.select(invocation);
    }

    @Override
    public void refresh(final Collection<Invoker<T>> invokers) {
        hmilyRoundRobinLoadBalance.refresh(invokers);
    }

}
