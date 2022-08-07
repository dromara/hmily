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

import org.dromara.hmily.core.context.XaParticipant;
import org.dromara.hmily.xa.rpc.RpcResource;
import org.dromara.hmily.xa.rpc.RpcXaProxy;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.lang.reflect.Method;

public class SpringCloudXaResource extends RpcResource {

    public SpringCloudXaResource(final Method method, final Object target, final Object[] args) {
        super(new SpringCloudXaProxy(method, target, args));
    }

    @Override
    public String getName() {
        return "springcloud";
    }

    @Override
    public void end(final Xid xid, final int i) throws XAException {

    }

    @Override
    public void forget(final Xid xid) throws XAException {

    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return getXaProxy().getTimeout();
    }

    @Override
    public boolean isSameRM(final XAResource xaResource) throws XAException {
        if (xaResource instanceof SpringCloudXaResource) {
            return ((SpringCloudXaResource) xaResource).getXaProxy().equals(this.getXaProxy());
        }
        return false;
    }

    @Override
    public boolean setTransactionTimeout(final int i) throws XAException {
        return true;
    }

    @Override
    public void start(final Xid xid, final int i) throws XAException {
        XaParticipant xaParticipant = new XaParticipant();
        xaParticipant.setFlag(i);
        xaParticipant.setBranchId(new String(xid.getBranchQualifier()));
        xaParticipant.setGlobalId(new String(xid.getGlobalTransactionId()));
        xaParticipant.setCmd(RpcXaProxy.XaCmd.START.name());
        this.getXaProxy().init(xaParticipant);
    }
}
