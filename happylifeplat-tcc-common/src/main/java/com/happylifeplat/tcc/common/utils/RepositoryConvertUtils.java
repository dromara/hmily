/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.happylifeplat.tcc.common.utils;

import com.happylifeplat.tcc.common.bean.adapter.CoordinatorRepositoryAdapter;
import com.happylifeplat.tcc.common.bean.entity.Participant;
import com.happylifeplat.tcc.common.bean.entity.TccTransaction;
import com.happylifeplat.tcc.common.exception.TccException;
import com.happylifeplat.tcc.common.serializer.ObjectSerializer;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/11/7 15:12
 * @since JDK 1.8
 */
public class RepositoryConvertUtils {


    public static byte[] convert(TccTransaction tccTransaction,
                                 ObjectSerializer objectSerializer) throws TccException {
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
    public static  TccTransaction transformBean(byte[] contents,
                                                ObjectSerializer objectSerializer) throws TccException {


        TccTransaction tccTransaction = new TccTransaction();

        final CoordinatorRepositoryAdapter adapter =
                objectSerializer.deSerialize(contents, CoordinatorRepositoryAdapter.class);

        List<Participant> participants =
                objectSerializer.deSerialize(adapter.getContents(), List.class);

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
