/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dromara.hmily.config.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.exception.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Nacos client.
 *
 * @author xiaoyu
 * @author sixh
 */
public class NacosClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosClient.class);
    
    private static final String NACOS_SERVER_ADDR_KEY = "serverAddr";
    
    /**
     * Pull input stream.
     *
     * @param config the config
     * @return the input stream
     */
    public InputStream pull(final NacosConfig config) {
        Properties properties = new Properties();
        properties.put(NACOS_SERVER_ADDR_KEY, config.getServer());
        try {
            ConfigService configService = NacosFactory.createConfigService(properties);
            String content = configService.getConfig(config.getDataId(), config.getGroup(), config.getTimeoutMs());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("nacos content {}", content);
            }
            if (StringUtils.isBlank(content)) {
                return null;
            }
            return new ByteArrayInputStream(content.getBytes());
        } catch (NacosException e) {
            throw new ConfigException(e);
        }
    }
}
