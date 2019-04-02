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

package org.dromara.hmily.common.utils;

import org.dromara.hmily.common.bean.adapter.CoordinatorRepositoryAdapter;
import org.dromara.hmily.common.bean.entity.HmilyParticipant;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.exception.HmilyException;
import org.dromara.hmily.common.serializer.ObjectSerializer;
import org.dromara.hmily.common.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RepositoryConvertUtils.
 *
 * @author xiaoyu(Myth)
 */
public class RepositoryConvertUtils {

    /**
     * Convert byte [ ].
     *
     * @param hmilyTransaction   the tcc transaction
     * @param objectSerializer the object serializer
     * @return the byte [ ]
     * @throws HmilyException the tcc exception
     */
    public static byte[] convert(final HmilyTransaction hmilyTransaction, final ObjectSerializer objectSerializer) throws HmilyException {
        CoordinatorRepositoryAdapter adapter = new CoordinatorRepositoryAdapter();
        adapter.setTransId(hmilyTransaction.getTransId());
        adapter.setLastTime(hmilyTransaction.getLastTime());
        adapter.setCreateTime(hmilyTransaction.getCreateTime());
        adapter.setRetriedCount(hmilyTransaction.getRetriedCount());
        adapter.setStatus(hmilyTransaction.getStatus());
        adapter.setTargetClass(hmilyTransaction.getTargetClass());
        adapter.setTargetMethod(hmilyTransaction.getTargetMethod());
        adapter.setPattern(hmilyTransaction.getPattern());
        adapter.setRole(hmilyTransaction.getRole());
        adapter.setVersion(hmilyTransaction.getVersion());
        if (CollectionUtils.isNotEmpty(hmilyTransaction.getHmilyParticipants())) {
            final HmilyParticipant hmilyParticipant = hmilyTransaction.getHmilyParticipants().get(0);
            adapter.setConfirmMethod(hmilyParticipant.getConfirmHmilyInvocation().getMethodName());
            adapter.setCancelMethod(hmilyParticipant.getCancelHmilyInvocation().getMethodName());
        }
        adapter.setContents(objectSerializer.serialize(hmilyTransaction.getHmilyParticipants()));
        return objectSerializer.serialize(adapter);
    }

    /**
     * Transform bean tcc transaction.
     *
     * @param contents         the contents
     * @param objectSerializer the object serializer
     * @return the tcc transaction
     * @throws HmilyException the tcc exception
     */
    @SuppressWarnings("unchecked")
    public static HmilyTransaction transformBean(final byte[] contents, final ObjectSerializer objectSerializer) throws HmilyException {
        HmilyTransaction hmilyTransaction = new HmilyTransaction();
        final CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(contents, CoordinatorRepositoryAdapter.class);
        List<HmilyParticipant> hmilyParticipants = objectSerializer.deSerialize(adapter.getContents(), ArrayList.class);
        hmilyTransaction.setLastTime(adapter.getLastTime());
        hmilyTransaction.setRetriedCount(adapter.getRetriedCount());
        hmilyTransaction.setCreateTime(adapter.getCreateTime());
        hmilyTransaction.setTransId(adapter.getTransId());
        hmilyTransaction.setStatus(adapter.getStatus());
        hmilyTransaction.setHmilyParticipants(hmilyParticipants);
        hmilyTransaction.setRole(adapter.getRole());
        hmilyTransaction.setPattern(adapter.getPattern());
        hmilyTransaction.setTargetClass(adapter.getTargetClass());
        hmilyTransaction.setTargetMethod(adapter.getTargetMethod());
        hmilyTransaction.setVersion(adapter.getVersion());
        return hmilyTransaction;
    }

}
