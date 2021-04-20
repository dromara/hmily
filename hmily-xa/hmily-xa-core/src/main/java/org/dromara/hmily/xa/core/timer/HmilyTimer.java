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
 * 实现一个关于时间的过期的缓存；当缓存的对象到期后，
 * 自动推送数据给相应的监听器。进行后续处理.
 *
 * @param <V> the type parameter
 * @author chenbin
 */
public class HmilyTimer<V> {

    /**
     * 过期时间.
     */
    private final long expire;

    /**
     * 环形时间队列.
     */
    private final HashedWheelTimer timer;

    /**
     * 增加监听器.
     */
    private TimerRemovalListener<V> timerRemovalListener;

    /**
     * 超时的时间单位.
     */
    private final TimeUnit unit;

    /**
     * 生成一个过期缓存对象.
     *
     * @param expire    执行；
     * @param unit      时间单位；
     * @param cacheName 设置一个缓存的名字；
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
