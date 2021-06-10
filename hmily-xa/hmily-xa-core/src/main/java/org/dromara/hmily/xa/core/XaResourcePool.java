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

package org.dromara.hmily.xa.core;

import javax.transaction.xa.Xid;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XaResourcePool .
 * 事务的resource存放.
 * 注意在完成的时候需要进行移除.
 * 保存jvm中存一个.
 *
 * @author sixh chenbin
 */
public class XaResourcePool {

    /**
     * The constant INST.
     */
    public static final XaResourcePool INST = new XaResourcePool();

    /**
     * 保存当前事务的资源.
     */
    private final Map<Xid, XaResourceWrapped> pool = new ConcurrentHashMap<>();

    private XaResourcePool() {

    }

    /**
     * Add resource.
     *
     * @param xid               the xid
     * @param xaResourceWrapped the xa resource wrapped
     */
    public void addResource(final Xid xid, final XaResourceWrapped xaResourceWrapped) {
        pool.put(xid, xaResourceWrapped);
    }

    /**
     * Remove resource xa resource wrapped.
     *
     * @param xid the xid
     * @return the xa resource wrapped
     */
    public XaResourceWrapped removeResource(final Xid xid) {
        return pool.remove(xid);
    }

    /**
     * Gets resource.
     *
     * @param xid the xid
     * @return the resource
     */
    public XaResourceWrapped getResource(final Xid xid) {
        XaResourceWrapped xaResourceWrapped = pool.get(xid);
        if (xaResourceWrapped == null) {
            //todo:从日志中查找.
        }
        return xaResourceWrapped;
    }
}
