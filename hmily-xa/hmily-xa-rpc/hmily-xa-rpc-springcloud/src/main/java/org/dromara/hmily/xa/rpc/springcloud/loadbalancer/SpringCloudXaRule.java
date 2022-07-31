///*
// * Copyright 2017-2021 Dromara.org
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.dromara.hmily.xa.rpc.springcloud.loadbalancer;
//
//import com.netflix.client.ClientFactory;
//import com.netflix.client.IClientConfigAware;
//import com.netflix.client.config.IClientConfig;
//import com.netflix.loadbalancer.ILoadBalancer;
//import com.netflix.loadbalancer.IRule;
//import com.netflix.loadbalancer.Server;
//import org.dromara.hmily.core.context.HmilyContextHolder;
//import org.dromara.hmily.core.context.HmilyTransactionContext;
//import org.dromara.hmily.core.context.XaParticipant;
//import org.dromara.hmily.xa.core.XidImpl;
//import org.dromara.hmily.xa.rpc.RpcXaProxy;
//
//import javax.transaction.xa.Xid;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * 要保证XA事务的prepare、commit、rollback等请求路由到同一个server
// * TODO transaction event，因为这次rpc可能失败，所以不能直接remove，一直不remove也会内存泄漏
// */
//public class SpringCloudXaRule implements IRule {
//    private static final Map<Xid, Server> ROUTER_MAP = new ConcurrentHashMap<> ();
//    /**
//     * 在处理Zone的负载均衡时，IRule会被复制，因为这个类是底层IRule的包装类，所以它丢失了类型信息
//     */
//    private static final Map<String, String> CLIENT_RULE_CLASS_MAP = new ConcurrentHashMap<> ();
//    private IRule delegate;
//
//    public SpringCloudXaRule(IRule delegate) {
//        this.delegate = delegate;
//    }
//
//    @Override
//    public Server choose(Object key) {
//        HmilyTransactionContext context = HmilyContextHolder.get ();
//        Server server = delegate.choose (key);
//        if (context != null) {
//            XaParticipant participant = context.getXaParticipant ();
//            if (participant != null) {
//                String cmd = participant.getCmd ();
//                Xid xid = new XidImpl (participant.getGlobalId (), participant.getBranchId ());
//                if (RpcXaProxy.XaCmd.START.name ().equalsIgnoreCase (cmd)) {
//                    //保证同一个事务分支的rpc路由到同一个server
//                    ROUTER_MAP.put (xid, server);
//                } else {
//                    Server oldServer = ROUTER_MAP.get (xid);
//                    if (oldServer != null) server = oldServer;
//                }
//            }
//        }
//
//        return server;
//    }
//
//    @Override
//    public ILoadBalancer getLoadBalancer() {
//        return delegate.getLoadBalancer ();
//    }
//
//    @Override
//    public void setLoadBalancer(ILoadBalancer lb) {
//        delegate.setLoadBalancer (lb);
//    }
//
//}
