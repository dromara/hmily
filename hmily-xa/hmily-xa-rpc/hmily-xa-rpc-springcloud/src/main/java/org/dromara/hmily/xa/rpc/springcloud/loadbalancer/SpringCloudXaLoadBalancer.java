/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.xa.rpc.springcloud.loadbalancer;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.context.XaParticipant;
import org.dromara.hmily.xa.core.XidImpl;
import org.dromara.hmily.xa.rpc.RpcXaProxy;
import org.springframework.context.ApplicationEvent;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.transaction.xa.Xid;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 要保证XA事务的prepare、commit、rollback等路由到同一个server.
 */
public class SpringCloudXaLoadBalancer implements ILoadBalancer {
    /**
     * Store server routing.
     */
    private static final Map<Xid, Server> ROUTER_MAP = new ConcurrentHashMap<>();

    private final ILoadBalancer delegate;

    public SpringCloudXaLoadBalancer(final ILoadBalancer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addServers(final List<Server> newServers) {
        delegate.addServers(newServers);
    }

    @Override
    public Server chooseServer(final Object key) {
        HmilyTransactionContext context = HmilyContextHolder.get();
        Server server = delegate.chooseServer(key);
        if (server != null && context != null) {
            XaParticipant participant = context.getXaParticipant();
            if (participant != null) {
                String cmd = participant.getCmd();
                Xid xid = new XidImpl(participant.getGlobalId(), participant.getBranchId());
                if (RpcXaProxy.XaCmd.START.name().equalsIgnoreCase(cmd)) {
                    //保证同一个事务分支的rpc路由到同一个server
                    ROUTER_MAP.put(xid, server);
                } else {
                    Server oldServer = ROUTER_MAP.get(xid);
                    if (oldServer != null) {
                        server = oldServer;
                    }
                }
            }
        }

        return server;
    }

    @Override
    public void markServerDown(final Server server) {
        delegate.markServerDown(server);
    }

    @Override
    public List<Server> getServerList(final boolean availableOnly) {
        return delegate.getServerList(availableOnly);
    }

    @Override
    public List<Server> getReachableServers() {
        return delegate.getReachableServers();
    }

    @Override
    public List<Server> getAllServers() {
        return delegate.getAllServers();
    }


    /**
     * 事务结束时才remove，这样外部就可以多次commit重试.
     */
    static class TransactionEventListener {
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
        public void onTransactionFinished(@SuppressWarnings("unused") final ApplicationEvent event) {
            HmilyTransactionContext context = HmilyContextHolder.get();
            if (context != null) {
                Xid xid = new XidImpl(context.getXaParticipant().getGlobalId(),
                        context.getXaParticipant().getBranchId());
                ROUTER_MAP.remove(xid);
            }
        }
    }
}
