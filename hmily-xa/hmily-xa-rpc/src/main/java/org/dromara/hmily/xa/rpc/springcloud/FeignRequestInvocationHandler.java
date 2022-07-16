package org.dromara.hmily.xa.rpc.springcloud;

import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.xa.core.TransactionImpl;
import org.dromara.hmily.xa.core.TransactionManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;


public class FeignRequestInvocationHandler implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger (FeignRequestInvocationHandler.class);
    private final Object target;

    public FeignRequestInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Hmily hmily = method.getAnnotation (Hmily.class);
            if (Objects.isNull (hmily)) {
                return method.invoke (target, args);
            }
        } catch (Exception ex) {
            LogUtil.error (LOGGER, "hmily find method error {} ", ex::getMessage);
            return method.invoke (target, args);
        }

        Transaction transaction = TransactionManagerImpl.INST.getTransaction ();
        if (transaction instanceof TransactionImpl) {
            XAResource resource = new SpringCloudXaResource (method, target, args);
            try {
                ((TransactionImpl) transaction).doEnList (resource, XAResource.TMJOIN);
            } catch (SystemException | RollbackException e) {
                LOGGER.error (":", e);
                throw new RuntimeException ("hmily xa resource tm join err", e);
            }
        }
        return method.invoke (target, args);
    }


}
