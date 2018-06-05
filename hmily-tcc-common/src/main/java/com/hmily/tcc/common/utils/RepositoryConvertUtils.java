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

package com.hmily.tcc.common.utils;

import com.hmily.tcc.common.bean.adapter.CoordinatorRepositoryAdapter;
import com.hmily.tcc.common.bean.entity.Participant;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.exception.TccException;
import com.hmily.tcc.common.serializer.ObjectSerializer;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RepositoryConvertUtils.
 * @author xiaoyu(Myth)
 */
public class RepositoryConvertUtils {

    public static byte[] convert(final TccTransaction tccTransaction, final ObjectSerializer objectSerializer) throws TccException {
        CoordinatorRepositoryAdapter adapter = new CoordinatorRepositoryAdapter();
        adapter.setTransId(tccTransaction.getTransId());
        adapter.setLastTime(tccTransaction.getLastTime());
        adapter.setCreateTime(tccTransaction.getCreateTime());
        adapter.setRetriedCount(tccTransaction.getRetriedCount());
        adapter.setStatus(tccTransaction.getStatus());
        adapter.setTargetClass(tccTransaction.getTargetClass());
        adapter.setTargetMethod(tccTransaction.getTargetMethod());
        adapter.setPattern(tccTransaction.getPattern());
        adapter.setRole(tccTransaction.getRole());
        adapter.setVersion(tccTransaction.getVersion());
        if (CollectionUtils.isNotEmpty(tccTransaction.getParticipants())) {
            final Participant participant = tccTransaction.getParticipants().get(0);
            adapter.setConfirmMethod(participant.getConfirmTccInvocation().getMethodName());
            adapter.setCancelMethod(participant.getCancelTccInvocation().getMethodName());
        }
        adapter.setContents(objectSerializer.serialize(tccTransaction.getParticipants()));
        return objectSerializer.serialize(adapter);
    }

    @SuppressWarnings("unchecked")
    public static TccTransaction transformBean(final byte[] contents, final ObjectSerializer objectSerializer) throws TccException {
        TccTransaction tccTransaction = new TccTransaction();
        final CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(contents, CoordinatorRepositoryAdapter.class);
        List<Participant> participants = objectSerializer.deSerialize(adapter.getContents(), ArrayList.class);
        tccTransaction.setLastTime(adapter.getLastTime());
        tccTransaction.setRetriedCount(adapter.getRetriedCount());
        tccTransaction.setCreateTime(adapter.getCreateTime());
        tccTransaction.setTransId(adapter.getTransId());
        tccTransaction.setStatus(adapter.getStatus());
        tccTransaction.setParticipants(participants);
        tccTransaction.setRole(adapter.getRole());
        tccTransaction.setPattern(adapter.getPattern());
        tccTransaction.setTargetClass(adapter.getTargetClass());
        tccTransaction.setTargetMethod(adapter.getTargetMethod());
        tccTransaction.setVersion(adapter.getVersion());
        return tccTransaction;
    }

}
