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

package org.dromara.hmily.config.zookeeper;

import lombok.Getter;
import lombok.Setter;
import org.dromara.hmily.config.api.AbstractConfig;
import org.dromara.hmily.config.api.constant.PrefixConstants;
import org.dromara.hmily.spi.HmilySPI;

/**
 * The type Zookeeper config.
 *
 * @author xiaoyu
 */
@Getter
@Setter
@HmilySPI("remoteZookeeper")
public class ZookeeperConfig extends AbstractConfig {
    
    private String serverList;
    
    private int retryIntervalMilliseconds = 500;
    
    private int maxRetries = 3;
    
    private int timeToLiveSeconds = 60;
    
    private int operationTimeoutMilliseconds = 500;
    
    private String digest;
    
    private String fileExtension = "yml";
    
    private String path;
    
    private boolean update;
    
    private String updateFileName;
    
    @Override
    public String prefix() {
        return PrefixConstants.REMOTE_ZOOKEEPER;
    }
}
