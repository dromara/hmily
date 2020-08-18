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

package org.dromara.hmily.metrics.prometheus.impl.histogram;

import io.prometheus.client.Histogram;
import org.dromara.hmily.metrics.api.HistogramMetricsTracker;
import org.dromara.hmily.metrics.api.HistogramMetricsTrackerDelegate;
import org.dromara.hmily.metrics.enums.MetricsLabelEnum;

/**
 * Transaction latency histogram metrics tracker.
 *
 * @author xiaoyu
 */
public final class TransactionLatencyHistogramMetricsTracker implements HistogramMetricsTracker {
    
    private static final Histogram TRANSACTION_LATENCY = Histogram.build()
            .labelNames("type")
            .name("transaction_latency_histogram_millis").help("Transaction Latency Histogram Millis (ms)")
            .register();
    
    @Override
    public HistogramMetricsTrackerDelegate startTimer(final String... labelValues) {
        Histogram.Timer timer = TRANSACTION_LATENCY.labels(labelValues).startTimer();
        return new PrometheusHistogramMetricsTrackerDelegate(timer);
    }
    
    @Override
    public String metricsLabel() {
        return MetricsLabelEnum.TRANSACTION_LATENCY.getName();
    }
}

