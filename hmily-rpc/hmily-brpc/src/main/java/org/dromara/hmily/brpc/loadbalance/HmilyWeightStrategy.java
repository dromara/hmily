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

package org.dromara.hmily.brpc.loadbalance;

import com.baidu.brpc.client.CommunicationClient;
import com.baidu.brpc.loadbalance.WeightStrategy;
import com.baidu.brpc.protocol.Request;
import java.util.List;
import java.util.Set;

/**
 * The hmily brpc weight strategy load balance.
 *
 * @author liu·yu
 */
public class HmilyWeightStrategy extends WeightStrategy {

    @Override
    public CommunicationClient selectInstance(final Request request, final List<CommunicationClient> instances, final Set<CommunicationClient> selectedInstances) {
        CommunicationClient client = super.selectInstance(request, instances, selectedInstances);
        return HmilyLoadBalanceUtils.doSelect(client, instances);
    }
}
