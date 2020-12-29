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

package org.dromara.hmily.grpc.parameter;

import io.grpc.Metadata;

/**
 * GrpcHmilyContext.
 *
 * @author tydhot
 */
public class GrpcHmilyContext {

    public static final Metadata.Key<String> HMILY_META_DATA =
            Metadata.Key.of("hmily-meta", Metadata.ASCII_STRING_MARSHALLER);

    private static ThreadLocal<String> hmilyContext = new ThreadLocal<>();

    private static ThreadLocal<GrpcInvokeContext> hmilyClass = new ThreadLocal<>();

    private static ThreadLocal<Boolean> hmilyFailContext = new ThreadLocal<>();

    /**
     * get hmilyContext conext.
     *
     * @return ThreadLocal
     */
    public static ThreadLocal<String> getHmilyContext() {
        return hmilyContext;
    }

    /**
     * get hmilyClass conext.
     *
     * @return ThreadLocal
     */
    public static ThreadLocal<GrpcInvokeContext> getHmilyClass() {
        return hmilyClass;
    }

    /**
     * get hmilyFailContext conext.
     *
     * @return ThreadLocal
     */
    public static ThreadLocal<Boolean> getHmilyFailContext() {
        return hmilyFailContext;
    }


    /**
     * remove hmily conext after invoke.
     *
     */
    public static void removeAfterInvoke() {
        GrpcHmilyContext.getHmilyClass().remove();
    }
}
