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
 * TccConfig tcc配置文件.
 * @author xiaoyu
 */
@Data
public class TccConfig {


    /**
     * 资源后缀  此参数请填写  关于是事务存储路径.
     * 1 如果是表存储 这个就是表名后缀，其他方式存储一样.
     * 2 如果此参数不填写，那么会默认获取应用的applicationName.
     */
    private String repositorySuffix;

    /**
     * 提供不同的序列化对象.
     * {@linkplain com.hmily.tcc.common.enums.SerializeEnum}
     */
    private String serializer = "kryo";

    /**
     * 任务调度线程大小.
     */
    private int scheduledThreadMax = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 调度时间周期 单位秒.
     */
    private int scheduledDelay = 60;

    /**
     * 最大重试次数.
     */
    private int retryMax = 3;

    /**
     * 事务恢复间隔时间 单位秒（注意 此时间表示本地事务创建的时间多少秒以后才会执行）.
     */
    private int recoverDelayTime = 60;

    /**
     * 补偿存储类型.
     * {@linkplain RepositorySupportEnum}
     */
    private String repositorySupport = "db";

    /**
     * disruptor  bufferSize.
     */
    private int bufferSize = 1024;

    /**
     * db配置.
     */
    private TccDbConfig tccDbConfig;

    /**
     * mongo配置.
     */
    private TccMongoConfig tccMongoConfig;

    /**
     * redis配置.
     */
    private TccRedisConfig tccRedisConfig;

    /**
     * zookeeper配置.
     */
    private TccZookeeperConfig tccZookeeperConfig;

    /**
     * file配置.
     */
    private TccFileConfig tccFileConfig;

}
