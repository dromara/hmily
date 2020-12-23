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

package org.dromara.hmily.tac.core.lock;

import org.dromara.hmily.core.repository.HmilyRepositoryStorage;
import org.dromara.hmily.repository.spi.entity.HmilyLock;
import org.dromara.hmily.repository.spi.exception.HmilyLockConflictException;
import org.dromara.hmily.tac.core.cache.HmilyLockCacheManager;

import java.util.Collection;
import java.util.Optional;

/**
 * Hmily lock manager.
 *
 * @author zhaojun
 */
public enum HmilyLockManager {
    
    /**
     * Instance hmily lock manager.
     */
    INSTANCE;
    
    /**
     * Try acquire locks.
     *
     * @param hmilyLocks hmily locks
     */
    //TODO add timeout mechanism in future
    public void tryAcquireLocks(final Collection<HmilyLock> hmilyLocks) {
        for (HmilyLock each : hmilyLocks) {
            Optional<HmilyLock> hmilyLock = HmilyLockCacheManager.getInstance().get(each.getLockId());
            if (hmilyLock.isPresent()) {
                throw new HmilyLockConflictException(String.format("current record [%s] has locked by transaction:[%s]", each.getLockId(), hmilyLock.get().getTransId()));
            }
        }
        HmilyRepositoryStorage.writeHmilyLocks(hmilyLocks);
        hmilyLocks.forEach(lock -> HmilyLockCacheManager.getInstance().cacheHmilyLock(lock.getLockId(), lock));
    }
    
    /**
     * Release locks.
     *
     * @param hmilyLocks hmily locks
     */
    public void releaseLocks(final Collection<HmilyLock> hmilyLocks) {
        HmilyRepositoryStorage.releaseHmilyLocks(hmilyLocks);
        hmilyLocks.forEach(lock -> HmilyLockCacheManager.getInstance().removeByKey(lock.getLockId()));
    }
}
