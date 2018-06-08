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

package com.hmily.tcc.core.coordinator;

import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.config.TccConfig;

/**
 * this is save transaction log service.
 * @author xiaoyu
 */
public interface CoordinatorService {

    /**
     * init hmily config.
     *
     * @param tccConfig {@linkplain TccConfig}
     * @throws Exception exception
     */
    void start(TccConfig tccConfig) throws Exception;

    /**
     * save tccTransaction.
     *
     * @param tccTransaction {@linkplain TccTransaction }
     * @return id
     */
    String save(TccTransaction tccTransaction);

    /**
     * find by transId.
     *
     * @param transId  transId
     * @return {@linkplain TccTransaction }
     */
    TccTransaction findByTransId(String transId);

    /**
     * remove transaction.
     *
     * @param id  transaction pk.
     * @return true success
     */
    boolean remove(String id);

    /**
     * update.
     * @param tccTransaction {@linkplain TccTransaction }
     */
    void update(TccTransaction tccTransaction);

    /**
     * update TccTransaction .
     * this is only update Participant field.
     * @param tccTransaction  {@linkplain TccTransaction }
     * @return rows
     */
    int updateParticipant(TccTransaction tccTransaction);

    /**
     * update TccTransaction status.
     * @param id  pk.
     * @param status   {@linkplain com.hmily.tcc.common.enums.TccActionEnum}
     * @return rows
     */
    int updateStatus(String id, Integer status);

}
