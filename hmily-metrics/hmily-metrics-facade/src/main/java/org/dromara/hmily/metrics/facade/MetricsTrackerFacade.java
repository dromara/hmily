/*
 * Copyright 2017-2021 Dromara.org

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

package org.dromara.hmily.metrics.facade;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.config.api.entity.HmilyMetricsConfig;
import org.dromara.hmily.metrics.spi.MetricsBootService;
import org.dromara.hmily.metrics.spi.MetricsRegister;
import org.dromara.hmily.spi.ExtensionLoaderFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Metrics tracker facade.
 */
@Slf4j
public final class MetricsTrackerFacade implements AutoCloseable {
    
    private MetricsBootService metricsBootService;
    
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    
    /**
     * Init for metrics tracker manager.
     *
     * @param metricsConfig metrics config
     */
    public void start(final HmilyMetricsConfig metricsConfig) {
        if (this.isStarted.compareAndSet(false, true)) {
            metricsBootService = ExtensionLoaderFactory.load(MetricsBootService.class, metricsConfig.getMetricsName());
            Preconditions.checkNotNull(metricsBootService,
                    "Can not find metrics tracker manager with metrics name : %s in metrics configuration.", metricsConfig.getMetricsName());
            metricsBootService.start(metricsConfig, ExtensionLoaderFactory.load(MetricsRegister.class, metricsConfig.getMetricsName()));
        } else {
            log.info("metrics tracker has started !");
        }
    }
    
    @Override
    public void close() {
        this.isStarted.compareAndSet(true, false);
        if (null != metricsBootService) {
            metricsBootService.stop();
        }
    }
}

