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

package org.dromara.hmily.config;

import lombok.Data;

/**
 * hmily config.
 *
 * @author xiaoyu
 */
@Data
public class HmilyConfig {

    /**
     * Resource suffix this parameter please fill in about is the transaction store path.
     * If it's a table store this is a table suffix, it's stored the same way.
     * If this parameter is not filled in, the applicationName of the application is retrieved by default
     */
    private String appName;
    
    /**
     * log serializer.
     */
    private String serializer = "kryo";
    
    /**
     * contextTransmittalMode.
     */
    private String contextTransmittalMode = "threadLocal";
    
    /**
     * scheduledPool Thread size.
     */
    private int scheduledThreadMax = Runtime.getRuntime().availableProcessors() << 1;
    
    /**
     * scheduledPool scheduledDelay unit SECONDS.
     */
    private int scheduledRecoveryDelay = 60;
    
    /**
     * scheduled clean delay.
     */
    private int scheduledCleanDelay = 60;
    
    /**
     * scheduledPhyDeletedDelay.
     */
    private int scheduledPhyDeletedDelay = 600;
    
    /**
     * scheduledPool scheduledInitDelay unit SECONDS.
     */
    private int scheduledInitDelay = 30;
    
    /**
     * recoverDelayTime Unit seconds
     * (note that this time represents how many seconds after the local transaction was created before execution).
     */
    private int recoverDelayTime = 60;
    
    /**
     * cleanDelayTime Unit seconds
     * (note that this time represents how many seconds after the local transaction was created before execution).
     */
    private int cleanDelayTime = 180;
    
    /**
     * query by limit.
     */
    private int limit = 100;
    
    /**
     * retry max.
     */
    private int retryMax = 10;
    
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
     * asyncRepository.
     */
    private boolean asyncRepository = true;
    
    /**
     * autoSql.
     */
    private boolean autoSql = true;
    
    /**
     * phyDeleted is true means physically deleted  is false means update status to death.
     */
    private boolean phyDeleted = true;
    
    /**
     * when phyDeleted is false , store days.
     */
    private int storeDays = 3;
    
    /**
     * repository.
     */
    private String repository = "mysql";
    
    /**
     * db config.
     */
    private HmilyDbConfig hmilyDbConfig;

    /**
     * mongo config.
     */
    private HmilyMongoConfig hmilyMongoConfig;

    /**
     * redis config.
     */
    private HmilyRedisConfig hmilyRedisConfig;
    
    /**
     * zookeeper config.
     */
    private HmilyZookeeperConfig hmilyZookeeperConfig;
    
    /**
     * file config.
     */
    private HmilyFileConfig hmilyFileConfig;
    
    /**
     * metrics config.
     */
    private HmilyMetricsConfig hmilyMetricsConfig;
}
