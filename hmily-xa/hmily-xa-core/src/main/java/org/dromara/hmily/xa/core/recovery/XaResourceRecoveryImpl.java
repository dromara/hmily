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

package org.dromara.hmily.xa.core.recovery;

import org.dromara.hmily.core.repository.HmilyRepositoryFacade;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.HmilyXaRepository;
import org.dromara.hmily.repository.spi.entity.HmilyXaRecovery;
import org.dromara.hmily.xa.core.XidImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * XaResourceRecoveryImpl .
 * 关于日志的处理.
 *
 * @author sixh chenbin
 */
public class XaResourceRecoveryImpl implements XaResourceRecovery {

    private static final Logger logger = LoggerFactory.getLogger(XaResourceRecoveryImpl.class);

    //get is repository.
    private volatile HmilyXaRepository repository;

    @Override
    public void commitLog(final RecoveryLog recoveryLog) {
        HmilyXaRecovery recovery = recoveryLog.getRecovery();
        repository().addLog(recovery);
    }

    @Override
    public void rollbackLog(final RecoveryLog recoveryLog) {
        HmilyXaRecovery recovery = recoveryLog.getRecovery();
        String tmUnique = recovery.getTmUnique();
        Integer state = recovery.getState();
        //恢复一下日志.
        List<HmilyXaRecovery> hmilyXaRecoveries = repository.queryByTmUnique(tmUnique, state);
        for (final HmilyXaRecovery hmilyXaRecovery : hmilyXaRecoveries) {

        }
    }

    @Override
    public void recover(String tmUnique, final XAResource resource) {
        if (resource == null) {
            return;
        }
        List<HmilyXaRecovery> hmilyXaRecoveries = repository.queryByTmUnique(tmUnique, 0);
    }

    /**
     * 查询当前的xids.
     */
    private List<XidImpl> recoverXids(XAResource xaResource) {
        //扫描需要恢复的事务.
        int flags = XAResource.TMSTARTRSCAN;
        List<XidImpl> xIds = new ArrayList<>();
        try {
            boolean done;
            do {
                Xid[] recover = xaResource.recover(flags);
                flags = XAResource.TMNOFLAGS;
                done = (recover == null || recover.length <= 0);
                if (!done) {
                    done = true;
                    for (final Xid xid : recover) {
                        XidImpl xidImpl = new XidImpl(xid);
                        if (!xIds.contains(xidImpl)) {
                            xIds.add(xidImpl);
                            done = false;
                        }
                    }
                }
            } while (!done);
        } catch (XAException xaException) {
            logger.warn("recover xids error", xaException);
            return Collections.emptyList();
        }
        return xIds;
    }

    private HmilyXaRepository repository() {
        if (repository == null) {
            synchronized (this) {
                if (repository == null) {
                    try {
                        HmilyRepository hmilyRepository1 = HmilyRepositoryFacade.getInstance().getHmilyRepository();
                        if (hmilyRepository1 instanceof HmilyXaRepository) {
                            repository = (HmilyXaRepository) hmilyRepository1;
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException("Repository 没有实现HmilyXaRepository接口.");
                    }
                }
            }
        }
        return repository;
    }
}
