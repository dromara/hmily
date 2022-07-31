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

package org.dromara.hmily.xa.rpc.springcloud;


import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.context.XaParticipant;
import org.dromara.hmily.xa.rpc.RpcXaProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class SpringCloudXaProxy implements RpcXaProxy {
    private final Logger logger = LoggerFactory.getLogger (SpringCloudXaProxy.class);

    private final Method method;
    private final Object target;
    private final Object[] args;
    private HmilyTransactionContext context;

    public SpringCloudXaProxy(Method method, Object target, Object[] args) {
        this.method = method;
        this.target = target;
        this.args = args;
    }

    public Method getMethod() {
        return method;
    }

    public Object getTarget() {
        return target;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public Integer cmd(XaCmd cmd, Map<String, Object> params) {
        if (cmd == null) {
            logger.warn ("cmd is null");
            return NO;
        }

        context.getXaParticipant ().setCmd (cmd.name ());
        HmilyContextHolder.set (context);
        try {
            method.invoke (target, args);
            return YES;
        } catch (Throwable e) {
            logger.error ("cmd {} err", cmd.name (), e);
            return EXC;
        }
    }

    @Override
    public int getTimeout() {
        return 0;//TODO
    }

    @Override
    public void init(XaParticipant participant) {
        context = new HmilyTransactionContext ();
        context.setXaParticipant (participant);
        HmilyContextHolder.set (context);
    }

    @Override
    public boolean equals(RpcXaProxy xaProxy) {
        if (xaProxy instanceof SpringCloudXaProxy) {
            SpringCloudXaProxy proxy = (SpringCloudXaProxy) xaProxy;
            return proxy.getMethod ().equals (getMethod ()) &&
                    proxy.getTarget ().equals (getTarget ()) &&
                    Arrays.equals (proxy.getArgs (), getArgs ());
        }
        return false;
    }

}
