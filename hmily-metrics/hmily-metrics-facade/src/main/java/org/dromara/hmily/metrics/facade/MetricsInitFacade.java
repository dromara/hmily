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

package org.dromara.hmily.metrics.facade;

import com.google.common.base.Preconditions;
import java.util.Optional;
import org.dromara.hmily.config.HmilyMetricsConfig;
import org.dromara.hmily.metrics.facade.handler.MetricsTrackerHandler;
import org.dromara.hmily.metrics.spi.MetricsInit;
import org.dromara.hmily.metrics.spi.MetricsTrackerManager;
import org.dromara.hmily.spi.ExtensionLoaderFactory;
import org.dromara.hmily.spi.HmilySPI;

/**
 * The type Metrics init facade.
 *
 * @author xiaoyu
 */
@HmilySPI(value = "metricsInit")
public class MetricsInitFacade implements MetricsInit {
    
    private static volatile boolean enabled;
    
    private static MetricsTrackerManager metricsTrackerManager;
    
    /**
     * Gets enabled.
     *
     * @return the enabled
     */
    public static boolean getEnabled() {
        return enabled;
    }
    
    @Override
    public void init(final HmilyMetricsConfig metricsConfig) {
        if (!enabled) {
            doInit(metricsConfig);
        }
    }
    
    @Override
    public void close() {
        if (!enabled) {
            return;
        }
        if (null != metricsTrackerManager) {
            metricsTrackerManager.stop();
        }
        MetricsTrackerHandler.getInstance().close();
        enabled = false;
    }
    
    private static void doInit(final HmilyMetricsConfig metricsConfig) {
        Preconditions.checkNotNull(metricsConfig, "metrics configuration can not be null.");
        metricsTrackerManager = ExtensionLoaderFactory.load(MetricsTrackerManager.class, metricsConfig.getMetricsName());
        Preconditions.checkNotNull(metricsTrackerManager, "Can not find metrics tracker manager with metrics name in metrics configuration.");
        metricsTrackerManager.start(metricsConfig);
        Integer threadCount = Optional.ofNullable(metricsConfig.getThreadCount()).orElse(Runtime.getRuntime().availableProcessors());
        MetricsTrackerHandler.getInstance().init(metricsConfig.isAsync(), threadCount, metricsTrackerManager);
        enabled = true;
    }
}
