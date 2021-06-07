/*
 * Copyright 2017-2021 Dromara.org
 *
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

package org.dromara.hmily.motan.loadbalance;

import com.google.common.collect.Maps;
import com.weibo.api.motan.rpc.Referer;
import com.weibo.api.motan.rpc.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;

/**
 * The type hmily load balance utils.
 *
 * @author xiaoyu(Myth)
 */
public class HmilyLoadBalanceUtils {

    private static final Map<String, URL> URL_MAP = Maps.newConcurrentMap();
    
    /**
     * Do select referer.
     *
     * @param <T>            the type parameter
     * @param defaultReferer the default referer
     * @param refererList    the referer list
     * @return the referer
     */
    public static <T> Referer<T> doSelect(final Referer<T> defaultReferer, final List<Referer<T>> refererList) {
        final HmilyTransactionContext hmilyTransactionContext = HmilyContextHolder.get();
        if (Objects.isNull(hmilyTransactionContext)) {
            return defaultReferer;
        }
        //if try
        String key = defaultReferer.getInterface().getName();
        if (hmilyTransactionContext.getAction() == HmilyActionEnum.TRYING.getCode()) {
            URL_MAP.put(key, defaultReferer.getUrl());
            return defaultReferer;
        }
        final URL orlUrl = URL_MAP.get(key);
        URL_MAP.remove(key);
        if (Objects.nonNull(orlUrl)) {
            for (Referer<T> inv : refererList) {
                if (Objects.equals(inv.getUrl(), orlUrl)) {
                    return inv;
                }
            }
        }
        return defaultReferer;
    }
}
