/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dromara.hmily.xa.rpc;

import org.dromara.hmily.xa.core.XaResourceWrapped;
import org.dromara.hmily.xa.core.XidImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import java.util.Collections;
import java.util.List;

/**
 * RpcResource .
 * Call remote service related resources.
 * <p>
 * 对于一些Rpc的调用事务。
 *
 * @author sixh chenbin
 */
public abstract class RpcResource extends XaResourceWrapped {

    private final RpcXaProxy xaProxy;

    private final Logger logger = LoggerFactory.getLogger(RpcResource.class);

    /**
     * Instantiates a new Rpc resource.
     *
     * @param xaProxy the xa proxy
     */
    public RpcResource(final RpcXaProxy xaProxy) {
        this.xaProxy = xaProxy;
    }

    @Override
    public void commit(final Xid xid, final boolean onePhase) throws XAException {
        super.commit(xid, onePhase);
        Object cmd = xaProxy.cmd(RpcXaProxy.XaCmd.COMMIT, Collections.emptyMap());
        exception(cmd);
    }

    @Override
    public int prepare(final Xid xid) throws XAException {
        Object cmd = xaProxy.cmd(RpcXaProxy.XaCmd.PREPARE, Collections.emptyMap());
        exception(cmd);
        return Integer.parseInt(cmd.toString());
    }

    @Override
    public Xid[] recover(final int i) throws XAException {
        Object cmd = xaProxy.cmd(RpcXaProxy.XaCmd.RECOVER, Collections.emptyMap());
        exception(cmd);
        if (cmd instanceof List) {
            List<?> cmdList = (List<?>) cmd;
            int l = cmdList.size();
            Xid[] xids = new XidImpl[l];
            for (int x = 0; x < l; x++) {
                XidImpl xid = new XidImpl(cmdList.get(x).toString());
                xids[x] = xid;
            }
            return xids;
        }
        return new Xid[0];
    }

    @Override
    public void rollback(final Xid xid) throws XAException {
        super.rollback(xid);
        Object cmd = xaProxy.cmd(RpcXaProxy.XaCmd.ROLLBACK, Collections.emptyMap());
        exception(cmd);
    }

    private void exception(Object cmd) throws XAException {
        if (cmd instanceof Integer) {
            int cmdInt = Integer.parseInt(cmd.toString());
            if (cmdInt < RpcXaProxy.YES) {
                logger.warn("xa exception:cmd :{}", cmd);
                throw new XAException(cmdInt);
            }
        }
    }

    /**
     * Gets xa proxy.
     *
     * @return the xa proxy
     */
    public RpcXaProxy getXaProxy() {
        return xaProxy;
    }
}
