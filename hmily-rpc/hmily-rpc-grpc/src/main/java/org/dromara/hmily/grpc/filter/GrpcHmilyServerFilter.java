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

package org.dromara.hmily.grpc.filter;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ForwardingServerCall;
import org.dromara.hmily.grpc.parameter.GrpcHmilyContext;

/**
 * Grpc server filter.
 *
 * @author tydhot
 */
public class GrpcHmilyServerFilter implements ServerInterceptor {

    @Override
    public <R, P> ServerCall.Listener<R> interceptCall(final ServerCall<R, P> serverCall,
                                                       final Metadata metadata, final ServerCallHandler<R, P> serverCallHandler) {
        GrpcHmilyContext.getHmilyContext().set(metadata.get(GrpcHmilyContext.HMILY_META_DATA));
        return serverCallHandler.startCall(new ForwardingServerCall.SimpleForwardingServerCall<R, P>(serverCall) {
            @Override
            public void sendMessage(final P message) {
                GrpcHmilyContext.getHmilyContext().remove();
                super.sendMessage(message);
            }
        }, metadata);
    }
}
