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

package org.dromara.hmily.core.cache;

import org.apache.commons.collections.CollectionUtils;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.config.HmilyConfig;
import org.dromara.hmily.core.coordinator.HmilyCoordinatorService;
import org.dromara.hmily.core.helper.SpringBeanUtils;
import org.dromara.hmily.core.service.HmilyApplicationService;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * The type Hmily transaction map db cache manager.
 *
 * @author xiaoyu(Myth)
 */
@Component
public class HmilyTransactionMapDbCacheManager implements DisposableBean, ApplicationListener<ContextRefreshedEvent> {

    private static final String FILE_NAME_SUFFIX = "-hmilyTransaction.db";

    private static final String MAP_NAME_SUFFIX = "HmilyTransactionMap";

    @Autowired
    private HmilyConfig hmilyConfig;

    @Autowired
    private HmilyApplicationService hmilyApplicationService;

    private DB db;

    private ConcurrentMap<String, HmilyTransaction> transactionMap;

    private HmilyCoordinatorService coordinatorService;

    /**
     * Put.
     *
     * @param hmilyTransaction the hmily transaction
     */
    public void put(final HmilyTransaction hmilyTransaction) {
        transactionMap.put(hmilyTransaction.getTransId(), hmilyTransaction);
        db.commit();
    }

    /**
     * Remove.
     *
     * @param id the id
     */
    public void remove(final String id) {
        transactionMap.remove(id);
        db.commit();
    }

    /**
     * Get hmily transaction.
     *
     * @param id the id
     * @return the hmily transaction
     */
    public HmilyTransaction get(final String id) {
        return Optional.ofNullable(transactionMap.get(id))
                .orElse(coordinatorService.findByTransId(id));
    }

    /**
     * Read all list.
     *
     * @return the list
     */
    public List<HmilyTransaction> readAll() {
        final Collection<HmilyTransaction> values = transactionMap.values();
        if (CollectionUtils.isNotEmpty(values)) {
            return new ArrayList<>(values);
        }
        return new ArrayList<>();
    }

    @Override
    public void destroy() {
        db.close();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        db = DBMaker.fileDB(buildFileName())
                .fileMmapEnableIfSupported()
                .fileMmapPreclearDisable()
                .cleanerHackEnable()
                .closeOnJvmShutdown()
                .transactionEnable()
                .concurrencyScale(hmilyConfig.getConcurrencyScale())
                .make();

        transactionMap = db.hashMap(buildMapName())
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
        coordinatorService = SpringBeanUtils.getInstance().getBean(HmilyCoordinatorService.class);
    }

    private String buildFileName() {
        return hmilyApplicationService.acquireName() + FILE_NAME_SUFFIX;
    }

    private String buildMapName() {
        return hmilyApplicationService.acquireName() + MAP_NAME_SUFFIX;
    }

}
