package org.dromara.hmily.tars.loadbalance;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.client.cluster.ServantInvokerAliveChecker;
import com.qq.tars.client.cluster.ServantInvokerAliveStat;
import com.qq.tars.client.rpc.InvokerComparator;
import com.qq.tars.client.rpc.loadbalance.LoadBalanceHelper;
import com.qq.tars.common.util.CollectionUtils;
import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.rpc.common.InvokeContext;
import com.qq.tars.rpc.common.Invoker;
import com.qq.tars.rpc.common.LoadBalance;
import com.qq.tars.rpc.common.exc.NoInvokerException;
import com.qq.tars.support.log.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * add HmilyHashLoadBalance.
 *
 * @author tydhot
 */
public class HmilyHashLoadBalance<T> implements LoadBalance<T> {

    private static final Logger LOGGER = LoggerFactory.getClientLogger();

    private final ServantProxyConfig config;

    private final InvokerComparator comparator = new InvokerComparator();

    private volatile List<Invoker<T>> sortedInvokersCache;

    private volatile List<Invoker<T>> staticWeightInvokersCache;

    public HmilyHashLoadBalance(final ServantProxyConfig config) {
        this.config = config;
    }

    /**
     * Use load balancing to select invoker.
     *
     * @param invocation invocation
     * @return Invoker
     * @throws NoInvokerException NoInvokerException
     */
    public Invoker<T> select(final InvokeContext invocation) throws NoInvokerException {
        long hash = Math.abs(StringUtils.convertLong(invocation.getAttachment(Constants.TARS_HASH), 0));

        List<Invoker<T>> staticWeightInvokers = staticWeightInvokersCache;
        if (staticWeightInvokers != null && !staticWeightInvokers.isEmpty()) {
            Invoker<T> invoker = staticWeightInvokers.get((int) (hash % staticWeightInvokers.size()));
            if (invoker.isAvailable()) {
                return invoker;
            }

            ServantInvokerAliveStat stat = ServantInvokerAliveChecker.get(invoker.getUrl());
            if (stat.isAlive() || (stat.getLastRetryTime() + (config.getTryTimeInterval() * 1000)) < System.currentTimeMillis()) {
                LOGGER.info("try to use inactive invoker|" + invoker.getUrl().toIdentityString());
                stat.setLastRetryTime(System.currentTimeMillis());
                return invoker;
            }
        }

        List<Invoker<T>> sortedInvokers = sortedInvokersCache;
        if (sortedInvokers == null || sortedInvokers.isEmpty()) {
            throw new NoInvokerException("no such active connection invoker");
        }

        List<Invoker<T>> list = new ArrayList<Invoker<T>>();
        for (Invoker<T> invoker : sortedInvokers) {
            if (!invoker.isAvailable()) {
                ServantInvokerAliveStat stat = ServantInvokerAliveChecker.get(invoker.getUrl());
                if (stat.isAlive() || (stat.getLastRetryTime() + (config.getTryTimeInterval() * 1000)) < System.currentTimeMillis()) {
                    list.add(invoker);
                }
            } else {
                list.add(invoker);
            }
        }
        //TODO When all is not available. Whether to randomly extract one
        if (list.isEmpty()) {
            throw new NoInvokerException(config.getSimpleObjectName() + " try to select active invoker, size=" + sortedInvokers.size() + ", no such active connection invoker");
        }

        Invoker<T> invoker = list.get((int) (hash % list.size()));

        if (!invoker.isAvailable()) {
            LOGGER.info("try to use inactive invoker|" + invoker.getUrl().toIdentityString());
            ServantInvokerAliveChecker.get(invoker.getUrl()).setLastRetryTime(System.currentTimeMillis());
        }

        return HmilyLoadBalanceUtils.doSelect(invoker, sortedInvokersCache);
    }

    /**
     * Refresh local invoker.
     *
     * @param invokers invokers
     */
    @Override
    public void refresh(final Collection<Invoker<T>> invokers) {
        LOGGER.info("{} try to refresh RoundRobinLoadBalance's invoker cache, size= {} ", config.getSimpleObjectName(), CollectionUtils.isEmpty(invokers) ? 0 : invokers.size());
        if (invokers == null || invokers.isEmpty()) {
            sortedInvokersCache = null;
            staticWeightInvokersCache = null;
            return;
        }

        List<Invoker<T>> sortedInvokersTmp = new ArrayList<Invoker<T>>(invokers);
        Collections.sort(sortedInvokersTmp, comparator);

        sortedInvokersCache = sortedInvokersTmp;
        staticWeightInvokersCache = LoadBalanceHelper.buildStaticWeightList(sortedInvokersTmp, config);

        LOGGER.info(config.getSimpleObjectName() + " refresh HashLoadBalance's invoker cache done, staticWeightInvokersCache size="
                + (staticWeightInvokersCache == null || staticWeightInvokersCache.isEmpty() ? 0 : staticWeightInvokersCache.size())
                + ", sortedInvokersCache size="
                + (sortedInvokersCache == null || sortedInvokersCache.isEmpty() ? 0 : sortedInvokersCache.size()));
    }

}
