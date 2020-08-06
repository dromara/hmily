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

package org.dromara.hmily.core.hook;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.SneakyThrows;
import org.dromara.hmily.common.utils.CollectionUtils;

/**
 * The type Hmily shutdown hook.
 *
 * @author xiaoyu
 */
public final class HmilyShutdownHook extends Thread {
    
    private static final HmilyShutdownHook INSTANCE = new HmilyShutdownHook("HmilyShutdownHook");

    private final Set<AutoCloseable> autoCloseableHashSet = new HashSet<>();

    private final AtomicBoolean destroyed = new AtomicBoolean(false);
    
    static {
        Runtime.getRuntime().addShutdownHook(INSTANCE);
    }

    private HmilyShutdownHook(final String name) {
        super(name);
    }
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static HmilyShutdownHook getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register auto closeable.
     *
     * @param autoCloseable the auto closeable
     */
    public void registerAutoCloseable(final AutoCloseable autoCloseable) {
        autoCloseableHashSet.add(autoCloseable);
    }

    @Override
    public void run() {
        closeAll();
    }

    @SneakyThrows
    private void closeAll() {
        if (!destroyed.compareAndSet(false, true) && CollectionUtils.isEmpty(autoCloseableHashSet)) {
            return;
        }
        for (AutoCloseable autoCloseable : autoCloseableHashSet) {
            autoCloseable.close();
        }
    }
}

