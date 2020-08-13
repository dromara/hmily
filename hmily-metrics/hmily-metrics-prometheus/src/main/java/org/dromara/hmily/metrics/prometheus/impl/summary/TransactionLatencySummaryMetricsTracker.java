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

package org.dromara.hmily.metrics.prometheus.impl.summary;

import io.prometheus.client.Summary;
import java.util.concurrent.TimeUnit;
import org.dromara.hmily.metrics.api.SummaryMetricsTracker;
import org.dromara.hmily.metrics.api.SummaryMetricsTrackerDelegate;
import org.dromara.hmily.metrics.enums.MetricsLabelEnum;

/**
 * Transaction latency summary metrics tracker.
 *
 * @author xiaoyu
 */
public final class TransactionLatencySummaryMetricsTracker implements SummaryMetricsTracker {
    
    private static final Summary TRANSACTION_LATENCY = Summary.build()
            .name("transaction_latency_summary_millis").labelNames("type")
            .help("Requests Latency Summary Millis (ms)")
            .quantile(0.5, 0.05)
            .quantile(0.95, 0.01)
            .quantile(0.99, 0.001)
            .maxAgeSeconds(TimeUnit.MINUTES.toSeconds(5))
            .ageBuckets(5)
            .register();
    
    @Override
    public SummaryMetricsTrackerDelegate startTimer(final String... labelValues) {
        Summary.Timer timer = TRANSACTION_LATENCY.labels(labelValues).startTimer();
        return new PrometheusSummaryMetricsTrackerDelegate(timer);
    }
    
    @Override
    public String metricsLabel() {
        return MetricsLabelEnum.TRANSACTION_LATENCY.getName();
    }
}

