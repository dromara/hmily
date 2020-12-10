/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.grpc.client;

import io.grpc.stub.AbstractStub;
import org.dromara.hmily.core.holder.SingletonHolder;
import org.dromara.hmily.grpc.parameter.GrpcHmilyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Grpc Hmily Client.
 *
 * @author tydhot
 */
public class GrpcHmilyClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcHmilyClient.class);

    /**
     * grpc sync.
     *
     * @param <T> T
     * @param abstractStub AbstractStub
     * @param method String
     * @param param Object
     * @param clazz Class
     * @return t T
     */
    public static <T> T syncInvoke(final AbstractStub abstractStub, final String method, final Object param, final Class<T> clazz) {
        GrpcHmilyContext.getHmilyParam().set(param);
        GrpcHmilyContext.getHmilyClass().set(abstractStub);

        if (SingletonHolder.INST.get(abstractStub.getClass()) == null) {
            SingletonHolder.INST.register(abstractStub.getClass(), abstractStub);
        }

        for (Method m : abstractStub.getClass().getMethods()) {
            if (m.getName().equals(method)) {
                try {
                    return (T) m.invoke(abstractStub, m.getParameterTypes()[0].cast(param));
                } catch (Exception e) {
                    LOGGER.error("failed to find the method invoke, exception is {}", e.getMessage());
                }
            }
        }
        return null;
    }

}
