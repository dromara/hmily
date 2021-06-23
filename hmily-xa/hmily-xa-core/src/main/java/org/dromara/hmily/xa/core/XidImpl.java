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

import com.google.common.base.Splitter;
import org.dromara.hmily.common.utils.NetUtils;

import javax.transaction.xa.Xid;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * XaId .
 *
 * @author sixh chenbin
 */
public class XidImpl implements Xid {

    private static final Integer DEF_ID = 8808;

    private static final AtomicLong XID = new AtomicLong(10000);

    /**
     * Process branch Id.
     */
    private static final AtomicLong BXID = new AtomicLong(1);

    private final String globalId;

    private final String branchId;

    private final byte[] globalIdByte;

    private final byte[] branchIdByte;

    /**
     * Instantiates a new X id.
     */
    public XidImpl() {
        Long id = XID.getAndIncrement();
        String bid = "0";
        String newId = id + "-" + bid + "-" + NetUtils.getLocalIp();
        this.globalId = newId;
        this.branchId = newId;
        this.branchIdByte = newId.getBytes();
        this.globalIdByte = newId.getBytes();
    }

    /**
     * Instantiates a new X id.
     *
     * @param xId the x id
     */
    public XidImpl(final XidImpl xId) {
        String bid;
        String gid;
        List<String> xxIdx = Splitter.on("-").splitToList(xId.getGlobalId());
        gid = xxIdx.get(0);
        bid = String.valueOf(BXID.getAndIncrement());
        String newId = gid + "-" + bid + "-" + NetUtils.getLocalIp();
        this.branchId = newId;
        this.branchIdByte = newId.getBytes();
        this.globalId = xId.getGlobalId();
        this.globalIdByte = xId.globalIdByte;
    }

    /**
     * Instantiates a new Xid.
     *
     * @param xidStr the xid str
     */
    public XidImpl(final String xidStr) {
        List<String> strings = Splitter.on("-").splitToList(xidStr);
        this.branchId = xidStr;
        this.branchIdByte = xidStr.getBytes();
        String gid = strings.get(0);
        this.globalId = gid;
        this.globalIdByte = gid.getBytes();
    }

    /**
     * Instantiates a new X id.
     *
     * @param xId   the x id
     * @param index the index
     */
    public XidImpl(final XidImpl xId, final Integer index) {
        String bid;
        String gid;
        List<String> xxIdx = Splitter.on("-").splitToList(xId.getGlobalId());
        gid = xxIdx.get(0);
        bid = String.valueOf(BXID.getAndIncrement());
        String newId = gid + "-" + bid + "-" + NetUtils.getLocalIp() + "-" + index;
        this.branchId = newId;
        this.branchIdByte = newId.getBytes();
        this.globalId = xId.getGlobalId();
        this.globalIdByte = xId.globalIdByte;
    }

    /**
     * Instantiates a new X id.
     *
     * @param xid the xid
     */
    public XidImpl(final Xid xid) {
        this.globalIdByte = xid.getGlobalTransactionId();
        this.branchIdByte = xid.getBranchQualifier();
        this.globalId = new String(xid.getGlobalTransactionId());
        this.branchId = new String(xid.getBranchQualifier());
    }


    /**
     * Gets global id.
     *
     * @return the global id
     */
    public String getGlobalId() {
        return globalId;
    }

    /**
     * Gets branch id.
     *
     * @return the branch id
     */
    public String getBranchId() {
        return branchId;
    }

    @Override
    public int getFormatId() {
        return DEF_ID;
    }

    @Override
    public byte[] getGlobalTransactionId() {
        return this.globalIdByte;
    }

    @Override
    public byte[] getBranchQualifier() {
        return this.branchIdByte;
    }

    /**
     * New branch id x id.
     *
     * @return the x id
     */
    public XidImpl newBranchId() {
        return new XidImpl(this);
    }

    /**
     * New res id x id.
     *
     * @param index the index
     * @return the x id
     */
    public XidImpl newResId(final int index) {
        return new XidImpl(this, index);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof XidImpl) {
            return ((XidImpl) obj).getGlobalId().equals(this.getGlobalId())
                    && ((XidImpl) obj).getBranchId().equals(this.getBranchId());
        }
        return false;
    }

    @Override
    public String toString() {
        return "XIdImpl{" +
                "globalId='" + globalId + '\'' +
                ", branchId='" + branchId + '\'' +
                '}';
    }
}
