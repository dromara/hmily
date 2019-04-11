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

package org.dromara.hmily.motan.loadbalance;

import com.google.common.collect.Maps;
import com.weibo.api.motan.cluster.loadbalance.RandomLoadBalance;
import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.core.extension.Activation;
import com.weibo.api.motan.core.extension.SpiMeta;
import com.weibo.api.motan.rpc.Referer;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.URL;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The type Motan hmily load balance.
 *
 * @param <T> the type parameter
 * @author xiaoyu(Myth)
 */
@SpiMeta(name = "hmily")
@Activation(key = {MotanConstants.NODE_TYPE_SERVICE, MotanConstants.NODE_TYPE_REFERER})
public class MotanHmilyLoadBalance<T> extends RandomLoadBalance<T> {

    private static final Map<String, URL> URL_MAP = Maps.newConcurrentMap();

    @Override
    protected Referer<T> doSelect(final Request request) {

        final Referer<T> referer = super.doSelect(request);

        final List<Referer<T>> refererList = getReferers();

        final HmilyTransactionContext hmilyTransactionContext = HmilyTransactionContextLocal.getInstance().get();

        if (Objects.isNull(hmilyTransactionContext)) {
            return referer;
        }

        final String transId = hmilyTransactionContext.getTransId();
        //if try
        if (hmilyTransactionContext.getAction() == HmilyActionEnum.TRYING.getCode()) {
            URL_MAP.put(transId, referer.getUrl());
            return referer;
        }

        final URL orlUrl = URL_MAP.get(transId);

        URL_MAP.remove(transId);

        if (Objects.nonNull(orlUrl)) {
            for (Referer<T> inv : refererList) {
                if (Objects.equals(inv.getUrl(), orlUrl)) {
                    return inv;
                }
            }
        }
        return referer;
    }

    @Override
    protected void doSelectToHolder(final Request request, final List<Referer<T>> refersHolder) {
        super.doSelectToHolder(request, refersHolder);
    }
}
