package org.dromara.hmily.xa.rpc.springcloud;


import org.dromara.hmily.core.context.XaParticipant;
import org.dromara.hmily.xa.core.timer.HashedWheelTimer;
import org.dromara.hmily.xa.rpc.RpcResource;
import org.dromara.hmily.xa.rpc.RpcXaProxy;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.lang.reflect.Method;

public class SpringCloudXaResource extends RpcResource {

    public SpringCloudXaResource(Method method, Object target, Object[] args) {
        super (new SpringCloudXaProxy (method, target, args));
    }

    @Override
    public String getName() {
        return "springcloud";
    }

    @Override
    public void end(Xid xid, int i) throws XAException {

    }

    @Override
    public void forget(Xid xid) throws XAException {

    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return getXaProxy().getTimeout();
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        if (xaResource instanceof SpringCloudXaResource) {
            return ((SpringCloudXaResource) xaResource).getXaProxy().equals(this.getXaProxy());
        }
        return false;
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        return true;
    }

    @Override
    public void start(final Xid xid, final int i) throws XAException {
        super.start(xid, i);

        XaParticipant xaParticipant = new XaParticipant();
        xaParticipant.setFlag(i);
        xaParticipant.setBranchId(new String(xid.getBranchQualifier()));
        xaParticipant.setGlobalId(new String(xid.getGlobalTransactionId()));
        xaParticipant.setCmd(RpcXaProxy.XaCmd.START.name());
        this.getXaProxy().init(xaParticipant);
    }
}
