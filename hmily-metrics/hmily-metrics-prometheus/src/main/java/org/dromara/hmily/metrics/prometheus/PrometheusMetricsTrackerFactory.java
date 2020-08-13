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

package org.dromara.hmily.metrics.prometheus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import org.dromara.hmily.metrics.api.MetricsTracker;
import org.dromara.hmily.metrics.api.MetricsTrackerFactory;
import org.dromara.hmily.metrics.prometheus.impl.counter.TransactionStatusCounterMetricsTracker;
import org.dromara.hmily.metrics.prometheus.impl.counter.TransactionTotalCounterMetricsTracker;
import org.dromara.hmily.metrics.prometheus.impl.histogram.TransactionLatencyHistogramMetricsTracker;
import org.dromara.hmily.metrics.prometheus.impl.summary.TransactionLatencySummaryMetricsTracker;

/**
 * Prometheus metrics tracker factory.
 *
 * @author xiaoyu
 */
public final class PrometheusMetricsTrackerFactory implements MetricsTrackerFactory {
    
    private static final Collection<MetricsTracker> REGISTER = new ArrayList<>();
    
    static {
        REGISTER.add(new TransactionTotalCounterMetricsTracker());
        REGISTER.add(new TransactionStatusCounterMetricsTracker());
        REGISTER.add(new TransactionLatencyHistogramMetricsTracker());
        REGISTER.add(new TransactionLatencySummaryMetricsTracker());
    }
    
    @Override
    public Optional<MetricsTracker> create(final String metricsType, final String metricsLabel) {
        return REGISTER.stream().filter(each -> each.metricsLabel().equals(metricsLabel) && each.metricsType().equals(metricsType)).findFirst();
    }
}

