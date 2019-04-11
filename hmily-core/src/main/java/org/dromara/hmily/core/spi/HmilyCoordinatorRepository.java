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

package org.dromara.hmily.core.spi;

import org.dromara.hmily.annotation.HmilySPI;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.config.HmilyConfig;
import org.dromara.hmily.common.serializer.ObjectSerializer;

import java.util.Date;
import java.util.List;

/**
 * CoordinatorRepository.
 * @author xiaoyu
 */
@HmilySPI
public interface HmilyCoordinatorRepository {

    int ROWS = 1;

    int FAIL_ROWS = 0;

    /**
     * create TccTransaction.
     *
     * @param hmilyTransaction {@linkplain HmilyTransaction}
     * @return rows 1
     */
    int create(HmilyTransaction hmilyTransaction);

    /**
     * delete TccTransaction.
     *
     * @param id  pk
     * @return rows 1
     */
    int remove(String id);

    /**
     * update TccTransaction.
     *
     * @param hmilyTransaction {@linkplain HmilyTransaction}
     * @return rows 1 success 0 fail
     */
    int update(HmilyTransaction hmilyTransaction);

    /**
     * update  participants.
     *
     * @param hmilyTransaction {@linkplain HmilyTransaction}
     * @return rows 1 success 0 fail
     */
    int updateParticipant(HmilyTransaction hmilyTransaction);


    /**
     * update status .
     * @param id  pk
     * @param status  status
     * @return rows 1 success 0 fail
     */
    int updateStatus(String id, Integer status);

    /**
     * acquired by id.
     *
     * @param id pk
     * @return {@linkplain HmilyTransaction}
     */
    HmilyTransaction findById(String id);

    /**
     * list all.
     *
     * @return {@linkplain HmilyTransaction}
     */
    List<HmilyTransaction> listAll();


    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行.
     *
     * @param date 延迟后的时间
     * @return {@linkplain HmilyTransaction}
     */
    List<HmilyTransaction> listAllByDelay(Date date);

    /**
     * init.
     *
     * @param modelName modelName
     * @param hmilyConfig {@linkplain HmilyConfig}
     */
    void init(String modelName, HmilyConfig hmilyConfig);

    /**
     * set scheme.
     *
     * @return scheme
     */
    String getScheme();

    /**
     * set objectSerializer.
     *
     * @param objectSerializer {@linkplain ObjectSerializer}
     */
    void setSerializer(ObjectSerializer objectSerializer);
}
