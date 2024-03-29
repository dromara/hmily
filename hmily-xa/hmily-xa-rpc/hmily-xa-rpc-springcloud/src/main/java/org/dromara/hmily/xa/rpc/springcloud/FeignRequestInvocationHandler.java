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

/**
 * 拦截Feign rpc，如果有事务，则为其创建一个{@link XAResource}.
 */
public class FeignRequestInvocationHandler implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeignRequestInvocationHandler.class);

    private final Object target;

    /**
     * 实例化一个FeignRequestInvocationHandler.
     * @param target 被FeignRequestInvocationHandler代理的目标对象.
     */
    public FeignRequestInvocationHandler(final Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        try {
            Hmily hmily = method.getAnnotation(Hmily.class);
            if (Objects.isNull(hmily)) {
                return method.invoke(target, args);
            }
        } catch (Exception ex) {
            LogUtil.error(LOGGER, "hmily find method error {} ", ex::getMessage);
            return method.invoke(target, args);
        }

        Transaction transaction = TransactionManagerImpl.INST.getTransaction();
        if (transaction instanceof TransactionImpl) {
            XAResource resource = new SpringCloudXaResource(method, target, args);
            try {
                ((TransactionImpl) transaction).doEnList(resource, XAResource.TMJOIN);
            } catch (SystemException | RollbackException e) {
                LOGGER.error(":", e);
                throw new RuntimeException("hmily xa resource tm join err", e);
            }
        }

        return method.invoke(target, args);
    }

}
