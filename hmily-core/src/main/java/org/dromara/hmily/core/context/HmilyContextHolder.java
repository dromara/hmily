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

package org.dromara.hmily.core.context;

import java.util.Objects;
import java.util.Optional;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.spi.ExtensionLoaderFactory;

/**
 * The type Hmily context holder.
 */
public class HmilyContextHolder {

    private static HmilyContext hmilyContext;

    static {
        HmilyConfig hmilyConfig = ConfigEnv.getInstance().getConfig(HmilyConfig.class);
        if (Objects.isNull(hmilyConfig)) {
            hmilyContext = new ThreadLocalHmilyContext();
        } else {
            hmilyContext = Optional.ofNullable(ExtensionLoaderFactory.load(HmilyContext.class, hmilyConfig.getContextTransmittalMode())).orElse(new ThreadLocalHmilyContext());
        }
    }

    /**
     * Set.
     *
     * @param context the context
     */
    public static void set(final HmilyTransactionContext context) {
        hmilyContext.set(context);
    }

    /**
     * Get hmily transaction context.
     *
     * @return the hmily transaction context
     */
    public static HmilyTransactionContext get() {
        return hmilyContext.get();
    }

    /**
     * Remove.
     */
    public static void remove() {
        hmilyContext.remove();
    }
}
