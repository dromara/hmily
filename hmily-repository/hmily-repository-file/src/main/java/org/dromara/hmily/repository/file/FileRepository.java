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

package org.dromara.hmily.repository.file;

import java.io.File;
import java.util.Date;
import java.util.List;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.config.HmilyConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.spi.HmilySPI;


/**
 * file impl.
 *
 * @author xiaoyu
 */
@SuppressWarnings("all")
@HmilySPI("file")
public class FileRepository implements HmilyRepository {
    
    private static volatile boolean initialized;
    
    private String filePath;
    
    private HmilySerializer hmilySerializer;
    
    
    @Override
    public void init(final HmilyConfig hmilyConfig) {
        filePath = hmilyConfig.getAppName();
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.mkdirs();
        }
    }
    
    @Override
    public void setSerializer(final HmilySerializer hmilySerializer) {
        this.hmilySerializer = hmilySerializer;
    }
    
    @Override
    public int createHmilyTransaction(HmilyTransaction hmilyTransaction) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public int updateRetryByLock(HmilyTransaction hmilyTransaction) {
        return 0;
    }
    
    @Override
    public HmilyTransaction findByTransId(Long transId) {
        return null;
    }
    
    @Override
    public List<HmilyTransaction> listLimitByDelay(Date date, int limit) {
        return null;
    }
    
    @Override
    public int updateHmilyTransactionStatus(Long transId, Integer status) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public int removeHmilyTransaction(Long transId) {
        return 0;
    }
    
    @Override
    public int removeHmilyTransactionByData(Date date) {
        return 0;
    }
    
    @Override
    public int createHmilyParticipant(HmilyParticipant hmilyParticipant) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public List<HmilyParticipant> findHmilyParticipant(Long participantId) {
        return null;
    }
    
    @Override
    public List<HmilyParticipant> listHmilyParticipant(Date date, String transType, int limit) {
        return null;
    }
    
    @Override
    public List<HmilyParticipant> listHmilyParticipantByTransId(Long transId) {
        return null;
    }
    
    @Override
    public boolean existHmilyParticipantByTransId(Long transId) {
        return false;
    }
    
    @Override
    public int updateHmilyParticipantStatus(Long participantId, Integer status) throws HmilyRepositoryException {
        return 0;
    }
    
    @Override
    public int removeHmilyParticipant(Long participantId) {
        return 0;
    }
    
    @Override
    public int removeHmilyParticipantByData(Date date) {
        return 0;
    }
    
    @Override
    public boolean lockHmilyParticipant(HmilyParticipant hmilyParticipant) {
        return false;
    }
    
    @Override
    public int createHmilyParticipantUndo(HmilyParticipantUndo hmilyParticipantUndo) {
        return 0;
    }
    
    @Override
    public List<HmilyParticipantUndo> findHmilyParticipantUndoByParticipantId(Long participantId) {
        return null;
    }
    
    @Override
    public int removeHmilyParticipantUndo(Long undoId) {
        return 0;
    }
    
    @Override
    public int removeHmilyParticipantUndoByData(Date date) {
        return 0;
    }
    
    @Override
    public int updateHmilyParticipantUndoStatus(Long undoId, Integer status) {
        return 0;
    }
    
    private void makeDir() {
        if (!initialized) {
            synchronized (FileRepository.class) {
                if (!initialized) {
                    File rootPathFile = new File(filePath);
                    if (!rootPathFile.exists()) {
                        boolean result = rootPathFile.mkdir();
                        if (!result) {
                            throw new HmilyRuntimeException("cannot create root path, the path to create is:" + filePath);
                        }
                        initialized = true;
                    } else if (!rootPathFile.isDirectory()) {
                        throw new HmilyRuntimeException("rootPath is not directory");
                    }
                }
            }
        }
    }
}
