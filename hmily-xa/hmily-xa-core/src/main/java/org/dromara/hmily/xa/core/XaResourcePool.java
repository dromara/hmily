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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * XaResourcePool .
 * 事务的resource存放.
 * 注意在完成的时候需要进行移除.
 * 保存jvm中存一个.
 *
 * @author sixh chenbin
 */
public final class XaResourcePool {

    /**
     * The constant INST.
     * 存放所有产生的resource，这样就可以commit或者rollback
     */
    public static final XaResourcePool INST = new XaResourcePool();

    /**
     * 保存当前事务的资源.
     */
    private final Map<Xid, XaResourceWrapped> pool = new ConcurrentHashMap<>();

    private final Map<String, Set<Xid>> xids = new ConcurrentHashMap<>();

    private XaResourcePool() {

    }

    /**
     * Add resource.
     *
     * @param xid               the xid
     * @param xaResourceWrapped the xa resource wrapped
     */
    public synchronized void addResource(final Xid xid, final XaResourceWrapped xaResourceWrapped) {
        pool.put(xid, xaResourceWrapped);
        //处理一下xid;
        String globalId = new String(xid.getGlobalTransactionId());
        Set<Xid> xids = this.xids.get(globalId);
        if (xids == null) {
            xids = new HashSet<>();
        }
        xids.add(xid);
        this.xids.put(globalId, xids);
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
     * Remove all.
     *
     * @param globalId the global id
     */
    public void removeAll(final String globalId) {
        Set<Xid> xids = this.xids.get(globalId);
        if (xids != null) {
            for (final Xid xid : xids) {
                removeResource(xid);
            }
            this.xids.remove(globalId);
        }
    }

    /**
     * Gets resource.
     *
     * @param xid the xid
     * @return the resource
     */
    public XaResourceWrapped getResource(final Xid xid) {
        XaResourceWrapped xaResourceWrapped = pool.get(xid);
//        if (xaResourceWrapped == null) {
//            //todo:从日志中查找.
//        }
        return xaResourceWrapped;
    }

    /**
     * Gets all resource.
     *
     * @param globalId the global id
     * @return the all resource
     */
    public List<XaResourceWrapped> getAllResource(final String globalId) {
        Set<Xid> xids = this.xids.get(globalId);
        if (xids != null) {
            return xids.stream().map(this::getResource).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
