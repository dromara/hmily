/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.core.repository.HmilyRepositoryStorage;
import org.dromara.hmily.repository.spi.entity.HmilyLock;
import org.dromara.hmily.repository.spi.exception.HmilyLockConflictException;
import org.dromara.hmily.tac.core.cache.HmilyLockCacheManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Hmily lock manager.
 *
 * @author zhaojun
 */
@Slf4j
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
        Set<String> existedHmilyLockIds = new HashSet<>();
        for (HmilyLock each : hmilyLocks) {
            Optional<HmilyLock> hmilyLock = HmilyLockCacheManager.getInstance().get(each.getLockId());
            if (hmilyLock.isPresent()) {
                if (!hmilyLock.get().getTransId().equals(each.getTransId())) {
                    String message = String.format("current record [%s] has locked by transaction:[%s]", each.getLockId(), hmilyLock.get().getTransId());
                    log.error(message);
                    throw new HmilyLockConflictException(message);
                }
                existedHmilyLockIds.add(hmilyLock.get().getLockId());
            }
        }
        Collection<HmilyLock> unrepeatedHmilyLocks = hmilyLocks;
        // If the lock already exists in the database, remove it from the hmilyLocks
        if (CollectionUtils.isNotEmpty(existedHmilyLockIds)) {
            unrepeatedHmilyLocks = hmilyLocks.stream().filter(lock -> !existedHmilyLockIds.contains(lock.getLockId())).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(unrepeatedHmilyLocks)) {
            return;
        }
        HmilyRepositoryStorage.writeHmilyLocks(unrepeatedHmilyLocks);
        unrepeatedHmilyLocks.forEach(lock -> HmilyLockCacheManager.getInstance().cacheHmilyLock(lock.getLockId(), lock));
    }
    
    /**
     * Release locks.
     *
     * @param hmilyLocks hmily locks
     */
    public void releaseLocks(final Collection<HmilyLock> hmilyLocks) {
        HmilyRepositoryStorage.releaseHmilyLocks(hmilyLocks);
        hmilyLocks.forEach(lock -> HmilyLockCacheManager.getInstance().removeByKey(lock.getLockId()));
        log.debug("TAC-release-lock ::: {}", hmilyLocks);
    }

    /**
     * Check locks.
     *
     * @param hmilyLocks hmily locks
     */
    public void checkLocks(final Collection<HmilyLock> hmilyLocks) {
        if (CollectionUtils.isEmpty(hmilyLocks)) {
            return;
        }
        for (HmilyLock lock : hmilyLocks) {
            Optional<HmilyLock> hmilyLock = HmilyLockCacheManager.getInstance().get(lock.getLockId());
            if (hmilyLock.isPresent() && !Objects.equals(hmilyLock.get().getTransId(), lock.getTransId())) {
                String message = String.format("current record [%s] has locked by transaction:[%s]", lock.getLockId(), hmilyLock.get().getTransId());
                log.error(message);
                throw new HmilyLockConflictException(message);
            }
        }
    }
}
