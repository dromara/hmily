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

package org.dromara.hmily.core.coordinator;

import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.config.HmilyConfig;
import org.dromara.hmily.common.enums.HmilyActionEnum;

/**
 * this is save transaction log service.
 * @author xiaoyu
 */
public interface HmilyCoordinatorService {

    /**
     * init hmily config.
     *
     * @param hmilyConfig {@linkplain HmilyConfig}
     * @throws Exception exception
     */
    void start(HmilyConfig hmilyConfig) throws Exception;

    /**
     * save tccTransaction.
     *
     * @param hmilyTransaction {@linkplain HmilyTransaction }
     * @return id
     */
    String save(HmilyTransaction hmilyTransaction);

    /**
     * find by transId.
     *
     * @param transId  transId
     * @return {@linkplain HmilyTransaction }
     */
    HmilyTransaction findByTransId(String transId);

    /**
     * remove transaction.
     *
     * @param id  transaction pk.
     * @return true success
     */
    boolean remove(String id);

    /**
     * update.
     * @param hmilyTransaction {@linkplain HmilyTransaction }
     */
    void update(HmilyTransaction hmilyTransaction);

    /**
     * update TccTransaction .
     * this is only update Participant field.
     * @param hmilyTransaction  {@linkplain HmilyTransaction }
     * @return rows
     */
    int updateParticipant(HmilyTransaction hmilyTransaction);

    /**
     * update TccTransaction status.
     * @param id  pk.
     * @param status   {@linkplain HmilyActionEnum}
     * @return rows
     */
    int updateStatus(String id, Integer status);

}
