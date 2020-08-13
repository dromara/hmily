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

import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.metrics.api.HistogramMetricsTrackerDelegate;
import org.dromara.hmily.metrics.api.SummaryMetricsTrackerDelegate;
import org.dromara.hmily.metrics.facade.handler.MetricsTrackerHandler;
import org.dromara.hmily.metrics.spi.MetricsHandlerFacade;

/**
 * Metrics tracker facade.
 *
 * @author xiaoyu
 */
@Slf4j
public final class MetricsTrackerHandlerFacade implements MetricsHandlerFacade {
    
    @Override
    public void counterIncrement(final String metricsLabel, final String... labelValues) {
        if (MetricsInitFacade.getEnabled()) {
            MetricsTrackerHandler.getInstance().counterInc(metricsLabel, labelValues);
        }
    }
    
    @Override
    public void gaugeIncrement(final String metricsLabel, final String... labelValues) {
        if (MetricsInitFacade.getEnabled()) {
            MetricsTrackerHandler.getInstance().gaugeInc(metricsLabel, labelValues);
        }
    }
    
    @Override
    public void gaugeDecrement(final String metricsLabel, final String... labelValues) {
        if (MetricsInitFacade.getEnabled()) {
            MetricsTrackerHandler.getInstance().gaugeDec(metricsLabel, labelValues);
        }
    }
    
    @Override
    public Supplier<Boolean> histogramStartTimer(final String metricsLabel, final String... labelValues) {
        if (!MetricsInitFacade.getEnabled()) {
            return () -> false;
        }
        Optional<HistogramMetricsTrackerDelegate> histogramMetricsTrackerDelegate = MetricsTrackerHandler.getInstance().histogramStartTimer(metricsLabel, labelValues);
        return () -> {
            histogramMetricsTrackerDelegate.ifPresent(this::histogramObserveDuration);
            return true;
        };
    }
    
    private void histogramObserveDuration(final HistogramMetricsTrackerDelegate delegate) {
        if (MetricsInitFacade.getEnabled()) {
            MetricsTrackerHandler.getInstance().histogramObserveDuration(delegate);
        }
    }
    
    @Override
    public Supplier<Boolean> summaryStartTimer(final String metricsLabel, final String... labelValues) {
        if (!MetricsInitFacade.getEnabled()) {
            return () -> false;
        }
        Optional<SummaryMetricsTrackerDelegate> optionalSummaryMetricsTrackerDelegate = MetricsTrackerHandler.getInstance().summaryStartTimer(metricsLabel, labelValues);
        return () -> {
            optionalSummaryMetricsTrackerDelegate.ifPresent(this::summaryObserveDuration);
            return true;
        };
    }
    
    private void summaryObserveDuration(final SummaryMetricsTrackerDelegate delegate) {
        if (MetricsInitFacade.getEnabled()) {
            MetricsTrackerHandler.getInstance().summaryObserveDuration(delegate);
        }
    }
}

