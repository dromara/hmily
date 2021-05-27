/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.dromara.hmily.xa.core.timer;


import org.dromara.hmily.common.concurrent.HmilyThreadFactory;

import java.util.concurrent.TimeUnit;

/**
 * Implement an expired cache about time; when the cached object expires,
 * automatically push data to the corresponding listener. Perform follow-up processing.
 *
 * @param <V> the type parameter
 * @author chenbin
 */
public class HmilyTimer<V> {

    /**
     * expire date.
     */
    private final long expire;

    /**
     * circular time queue.
     */
    private final HashedWheelTimer timer;

    /**
     * add listener.
     */
    private TimerRemovalListener<V> timerRemovalListener;

    /**
     * Time unit of timeout.
     */
    private final TimeUnit unit;

    /**
     * Generate an expired cache object.
     *
     * @param expire    carried out；
     * @param unit      time unit；
     * @param cacheName cache name；
     */
    public HmilyTimer(long expire, TimeUnit unit, String cacheName) {
        this.expire = expire;
        timer = new HashedWheelTimer(HmilyThreadFactory.create(cacheName, false));
        this.unit = unit;
    }

    /**
     * 增加一个缓存移除的监听器.
     *
     * @param listener 监听器；
     */
    public void addRemovalListener(TimerRemovalListener<V> listener) {
        this.timerRemovalListener = listener;
    }

    /**
     * 接收缓存变化值.
     *
     * @param v value
     * @return timeout timeout
     * @see Timeout
     */
    public Timeout put(V v) {
        return put(v, this.expire, this.unit);
    }

    /**
     * 接收缓存变化值 ,灵活指定过期时间.
     *
     * @param v      value
     * @param expire 过期时间；
     * @param unit   单位；
     * @return Timeout timeout
     */
    public Timeout put(V v, long expire, TimeUnit unit) {
        Node node = new Node(v, expire, unit);
        return timer.newTimeout(node, expire, unit);
    }

    /**
     * 保存.
     */
    public class Node implements TimerTask {
        /**
         * 缓存的value.
         */
        private final V value;

        /**
         * 开始运行的时间.
         */
        private final Long time;

        /**
         * 默认等待的时间.
         */
        private final Long expire;

        /**
         * 默认等时间的单位.
         */
        private final TimeUnit unit;

        /**
         * 初始化一个对象.
         *
         * @param value  缓存的值 ;
         * @param expire 默认等待时间;
         * @param unit   默认等时间的单位;
         */
        public Node(V value, long expire, TimeUnit unit) {
            this.value = value;
            time = System.nanoTime();
            this.expire = expire;
            this.unit = unit;
        }

        /**
         * get cache value.
         *
         * @return v. value
         */
        public V getValue() {
            return value;
        }

        /**
         * get time.
         *
         * @return long. time
         */
        public Long getTime() {
            return time;
        }

        @Override
        public void run(Timeout timeout) {
            if (HmilyTimer.this.timerRemovalListener != null) {
                long elapsed = System.nanoTime() - time;
                long sd = unit.toMillis(expire);
                long ss = TimeUnit.NANOSECONDS.toMillis(elapsed);
                HmilyTimer.this.timerRemovalListener.onRemoval(value, sd, ss);
            }
        }
    }

}
