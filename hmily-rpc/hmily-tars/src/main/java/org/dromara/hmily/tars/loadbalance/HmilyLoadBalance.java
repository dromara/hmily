package org.dromara.hmily.tars.loadbalance;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.StringUtils;
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

    private final ServantProxyConfig config;

    private volatile Collection<Invoker<T>> lastRefreshInvokers;

    private volatile HmilyHashLoadBalance<T> hashLoadBalance;

    private final Object hashLoadBalanceLock = new Object();

    private volatile HmilyConsistentHashLoadBalance<T> consistentHashLoadBalance;

    private final Object consistentHashLoadBalanceLock = new Object();

    public HmilyLoadBalance(final HmilyRoundRobinLoadBalance hmilyRoundRobinLoadBalance, final ServantProxyConfig config) {
        this.hmilyRoundRobinLoadBalance = hmilyRoundRobinLoadBalance;
        this.config = config;
    }

    @Override
    public Invoker<T> select(final InvokeContext invocation) throws NoInvokerException {
        long hash = Math.abs(StringUtils.convertLong(invocation.getAttachment(Constants.TARS_HASH), 0));
        long consistentHash = Math.abs(StringUtils.convertLong(invocation.getAttachment(Constants.TARS_CONSISTENT_HASH), 0));

        if (consistentHash > 0) {
            if (consistentHashLoadBalance == null) {
                synchronized (consistentHashLoadBalanceLock) {
                    if (consistentHashLoadBalance == null) {
                        HmilyConsistentHashLoadBalance<T> tmp = new HmilyConsistentHashLoadBalance<T>(config);
                        tmp.refresh(lastRefreshInvokers);
                        consistentHashLoadBalance = tmp;
                    }
                }
            }
            return consistentHashLoadBalance.select(invocation);
        }

        if (hash > 0) {
            if (hashLoadBalance == null) {
                synchronized (hashLoadBalanceLock) {
                    if (hashLoadBalance == null) {
                        HmilyHashLoadBalance<T> tmp = new HmilyHashLoadBalance<T>(config);
                        tmp.refresh(lastRefreshInvokers);
                        hashLoadBalance = tmp;
                    }
                }
            }
            return hashLoadBalance.select(invocation);
        }

        return hmilyRoundRobinLoadBalance.select(invocation);
    }

    @Override
    public void refresh(final Collection<Invoker<T>> invokers) {
        lastRefreshInvokers = invokers;

        synchronized (hashLoadBalanceLock) {
            if (hashLoadBalance != null) {
                hashLoadBalance.refresh(invokers);
            }
        }

        synchronized (consistentHashLoadBalanceLock) {
            if (consistentHashLoadBalance != null) {
                consistentHashLoadBalance.refresh(invokers);
            }
        }

        hmilyRoundRobinLoadBalance.refresh(invokers);
    }

}
