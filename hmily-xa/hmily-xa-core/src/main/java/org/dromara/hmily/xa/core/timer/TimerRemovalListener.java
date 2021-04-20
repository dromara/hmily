/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dromara.hmily.xa.core.timer;

/**
 * 缓存过期的通知.
 *
 * @param <V> the type parameter
 * @author chenbin
 */
public interface TimerRemovalListener<V> {
    /**
     * 通知能数.
     *
     * @param value   value
     * @param expire  默认等待时间;
     * @param elapsed 运行了多少时间;
     */
    void onRemoval(V value, Long expire, Long elapsed);
}