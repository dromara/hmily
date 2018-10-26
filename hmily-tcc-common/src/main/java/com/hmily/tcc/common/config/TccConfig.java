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

package com.hmily.tcc.common.config;

import com.hmily.tcc.common.enums.RepositorySupportEnum;
import lombok.Data;

/**
 * hmily config.
 *
 * @author xiaoyu
 */
@Data
public class TccConfig {


    /**
     * Resource suffix this parameter please fill in about is the transaction store path.
     * If it's a table store this is a table suffix, it's stored the same way.
     * If this parameter is not filled in, the applicationName of the application is retrieved by default
     */
    private String repositorySuffix;

    /**
     * log serializer.
     * {@linkplain com.hmily.tcc.common.enums.SerializeEnum}
     */
    private String serializer = "kryo";

    /**
     * scheduledPool Thread size.
     */
    private int scheduledThreadMax = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * scheduledPool scheduledDelay unit SECONDS.
     */
    private int scheduledDelay = 60;

    /**
     * retry max.
     */
    private int retryMax = 3;

    /**
     * recoverDelayTime Unit seconds
     * (note that this time represents how many seconds after the local transaction was created before execution).
     */
    private int recoverDelayTime = 60;

    /**
     * Parameters when participants perform their own recovery.
     * 1.such as RPC calls time out
     * 2.such as the starter down machine
     */
    private int loadFactor = 2;

    /**
     * repositorySupport.
     * {@linkplain RepositorySupportEnum}
     */
    private String repositorySupport = "db";

    /**
     * disruptor bufferSize.
     */
    private int bufferSize = 4096 * 2 * 2;

    /**
     * this is disruptor consumerThreads.
     */
    private int consumerThreads = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * this is hmily async execute cancel or confirm thread size.
     */
    private int asyncThreads = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * when start this set true  actor set false.
     */
    private Boolean started = true;

    /**
     * db config.
     */
    private TccDbConfig tccDbConfig;

    /**
     * mongo config.
     */
    private TccMongoConfig tccMongoConfig;

    /**
     * redis config.
     */
    private TccRedisConfig tccRedisConfig;

    /**
     * zookeeper config.
     */
    private TccZookeeperConfig tccZookeeperConfig;

    /**
     * file config.
     */
    private TccFileConfig tccFileConfig;

}
