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

package org.dromara.hmily.metrics.prometheus.impl.counter;

import io.prometheus.client.Counter;
import org.dromara.hmily.metrics.api.CounterMetricsTracker;
import org.dromara.hmily.metrics.enums.MetricsLabelEnum;

/**
 * Request total counter metrics tracker.
 */
public final class HttpRequestCounterMetricsTracker implements CounterMetricsTracker {
    
    private static final Counter HTTP_REQUEST_TOTAL = Counter.build()
            .name("http_request_total")
            .labelNames("path", "type")
            .help("soul http request type total count")
            .register();
    
    @Override
    public void inc(final double amount, final String... labelValues) {
        HTTP_REQUEST_TOTAL.labels(labelValues).inc(amount);
    }
    
    @Override
    public String metricsLabel() {
        return MetricsLabelEnum.HTTP_REQUEST_TOTAL.getName();
    }
}

