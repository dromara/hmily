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

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.config.api.Config;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.config.api.entity.HmilyFileConfig;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.serializer.jdk.JDKSerializer;
import org.dromara.hmily.serializer.kryo.KryoSerializer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * file repository test unit
 *
 * @author choviwu
 */
public class FileRepositoryTest {

    private FileRepository fileRepository = new FileRepository();

    private HmilyConfig hmilyConfig = new HmilyConfig();

    private HmilyFileConfig hmilyFileConfig = new HmilyFileConfig();

    private String appName = "test-hmily";

    private Random random = new Random(System.currentTimeMillis());

    /**
     * Sets up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        hmilyFileConfig.setPath(System.getProperty("hmily.file.path"));

        ConfigEnv.getInstance().putBean(hmilyFileConfig);
        hmilyConfig.setAppName(appName);

        fileRepository.init(appName);
        fileRepository.setSerializer(new KryoSerializer());
//        fileRepository.setSerializer(new JDKSerializer());
//        fileRepository.setSerializer(new JDKSerializer());
//        fileRepository.setSerializer(new JDKSerializer());
    }

    /**
     * Test curd.
     */
    @Test
    public void testCURD() throws Exception {
        Long transactionId = (long) random.nextInt(1000);
        testTransaction(transactionId);

        Long participantId = (long) random.nextInt(1000);
        testParticipant(transactionId, participantId);

        Long undoId = (long) random.nextInt(1000);
        testParticipantUndo(transactionId, participantId, undoId);
    }

    private void testTransaction(Long transactionId) throws Exception {
        HmilyTransaction hmilyTransaction = buildHmilyTransaction(transactionId);
        int result = fileRepository.createHmilyTransaction(hmilyTransaction);
        assertNotEquals(0L, result);

        int updateStatusResult = fileRepository.updateHmilyTransactionStatus(hmilyTransaction.getTransId(), 3);
        assertNotEquals(0L, updateStatusResult);

        hmilyTransaction.setTransType(TransTypeEnum.TAC.name());
        hmilyTransaction.setStatus(4);
        int updateLockResult = fileRepository.updateRetryByLock(hmilyTransaction);
        assertNotEquals(0L, updateLockResult);


        HmilyTransaction findTransactionResult = fileRepository.findByTransId(hmilyTransaction.getTransId());
        assertNotNull(findTransactionResult);
        assertEquals(appName, findTransactionResult.getAppName());
        assertEquals(TransTypeEnum.TAC.name(), findTransactionResult.getTransType());
        assertEquals(4L, findTransactionResult.getStatus());


        hmilyTransaction = buildHmilyTransaction((long) random.nextInt(1000));
        result = fileRepository.createHmilyTransaction(hmilyTransaction);
        assertEquals(1L, result);

        hmilyTransaction = buildHmilyTransaction((long) random.nextInt(1000));
        result = fileRepository.createHmilyTransaction(hmilyTransaction);
        assertEquals(1L, result);

        hmilyTransaction = buildHmilyTransaction((long) random.nextInt(1000));
        result = fileRepository.createHmilyTransaction(hmilyTransaction);
        assertEquals(1L, result);
        TimeUnit.SECONDS.sleep(2);
        Date recoveryDate = acquireDelayData(2);
        List<HmilyTransaction> listTransactionResult = fileRepository.listLimitByDelay(recoveryDate, 2);
        assertNotNull(listTransactionResult);
        assertNotEquals(0L, listTransactionResult.size());
        assertEquals(2L, listTransactionResult.size());
        int removeByIdResult = fileRepository.removeHmilyTransaction(transactionId);
        assertEquals(1L, removeByIdResult);

        TimeUnit.SECONDS.sleep(2);
        recoveryDate = acquireDelayData(2);
        int removeByDateResult = fileRepository.removeHmilyTransactionByData(recoveryDate);
        assertEquals(3L, removeByDateResult);
    }

    private void testParticipantUndo(Long transactionId, Long participantId, Long undoId) throws Exception {
        int result;
        HmilyParticipantUndo hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, undoId);
        result = fileRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, result);


        int updateStatusResult = fileRepository.updateHmilyParticipantUndoStatus(undoId, 3);
        assertEquals(1L, updateStatusResult);

        List<HmilyParticipantUndo> findUndoResult = fileRepository.findHmilyParticipantUndoByParticipantId(hmilyParticipantUndo.getParticipantId());
        assertNotNull(findUndoResult);
        assertNotEquals(0L, findUndoResult.size());
        assertEquals(findUndoResult.get(0).getUndoId(), hmilyParticipantUndo.getUndoId());
        assertEquals(3L, (long) findUndoResult.get(0).getStatus());


        int removeByIdResult = fileRepository.removeHmilyParticipantUndo(undoId);
        assertEquals(1L, removeByIdResult);

        hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, (long) random.nextInt(1000));
        result = fileRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, result);
        hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, (long) random.nextInt(1000));
        result = fileRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, result);
        hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, (long) random.nextInt(1000));
        result = fileRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, result);

        TimeUnit.SECONDS.sleep(2);
        Date recoveryDate = acquireDelayData(2);
        int removeByDateResult = fileRepository.removeHmilyParticipantUndoByData(recoveryDate);
        assertEquals(3L, removeByDateResult);
    }

    private void testParticipant(Long transactionId, Long participantId) throws Exception {
        HmilyParticipant hmilyParticipant = buildHmilyParticipant(transactionId, participantId);
        int result = fileRepository.createHmilyParticipant(hmilyParticipant);
        assertNotEquals(0L, result);


        int updateStatusResult = fileRepository.updateHmilyParticipantStatus(participantId, 5);
        assertEquals(1L, updateStatusResult);
        hmilyParticipant.setStatus(4);
        boolean lockUpdateResult = fileRepository.lockHmilyParticipant(hmilyParticipant);
        assertTrue(lockUpdateResult);

        List<HmilyParticipant> findParticipantResult = fileRepository.findHmilyParticipant(hmilyParticipant.getParticipantId());
        assertNotNull(findParticipantResult);
        assertNotEquals(0L, findParticipantResult.size());
        assertTrue(findParticipantResult.get(0).getParticipantId().equals(hmilyParticipant.getParticipantId()) ||
                findParticipantResult.get(0).getParticipantRefId().equals(hmilyParticipant.getParticipantId()));
        assertEquals(4L, (long) findParticipantResult.stream()
                .filter(x -> x.getParticipantId()
                        .equals(participantId)).findFirst().get().getStatus());

        long id1 = random.nextInt(1000);
        hmilyParticipant = buildHmilyParticipant(transactionId, id1);
        result = fileRepository.createHmilyParticipant(hmilyParticipant);
        assertEquals(1L, result);
        long id2 = random.nextInt(1000);
        hmilyParticipant = buildHmilyParticipant(transactionId, id2);
        result = fileRepository.createHmilyParticipant(hmilyParticipant);
        assertEquals(1L, result);
        long id3 = random.nextInt(1000);
        hmilyParticipant = buildHmilyParticipant(transactionId, id3);
        result = fileRepository.createHmilyParticipant(hmilyParticipant);
        assertEquals(1L, result);
        TimeUnit.SECONDS.sleep(2);
        Date recoveryDate = acquireDelayData(2);
        List<HmilyParticipant> listResult = fileRepository.listHmilyParticipant(recoveryDate, TransTypeEnum.TCC.name(), 2);
        assertEquals(2L, listResult.size());

        List<HmilyParticipant> listByTransactionIdResult = fileRepository.listHmilyParticipantByTransId(transactionId);
        assertEquals(4L, listByTransactionIdResult.size());

        boolean existsRsult = fileRepository.existHmilyParticipantByTransId(transactionId);
        assertTrue(existsRsult);

        int removeByIdResult = fileRepository.removeHmilyParticipant(participantId);
        assertEquals(1L, removeByIdResult);

        fileRepository.updateHmilyParticipantStatus(id1, 4);
        fileRepository.updateHmilyParticipantStatus(id2, 4);
        fileRepository.updateHmilyParticipantStatus(id3, 4);
        TimeUnit.SECONDS.sleep(2);
        recoveryDate = acquireDelayData(2);
        int removeByDateResult = fileRepository.removeHmilyParticipantByData(recoveryDate);
        assertEquals(3L, removeByDateResult);
    }

    private HmilyParticipantUndo buildHmilyParticipantUndo(Long transactionId, Long particaipantId, Long undoId) {
        HmilyParticipantUndo hmilyParticipantUndo = new HmilyParticipantUndo();
        hmilyParticipantUndo.setStatus(4);
        hmilyParticipantUndo.setTransId(transactionId);
        hmilyParticipantUndo.setParticipantId(particaipantId);
        hmilyParticipantUndo.setUndoId(undoId);
        return hmilyParticipantUndo;
    }

    private HmilyParticipant buildHmilyParticipant(Long transactionId, Long particaipantId) {
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setParticipantId(particaipantId);
        hmilyParticipant.setTransId(transactionId);
        hmilyParticipant.setAppName(appName);
        hmilyParticipant.setParticipantRefId((long) random.nextInt(1000));
        hmilyParticipant.setStatus(3);
        hmilyParticipant.setTransType(TransTypeEnum.TCC.name());
        return hmilyParticipant;
    }

    private HmilyTransaction buildHmilyTransaction(Long transactionId) {
        HmilyTransaction hmilyTransaction = new HmilyTransaction();
        hmilyTransaction.setAppName(appName);
        hmilyTransaction.setTransId(transactionId);
        hmilyTransaction.setStatus(4);
        hmilyTransaction.setRetry(0);
        hmilyTransaction.setTransType(TransTypeEnum.TCC.name());
        return hmilyTransaction;
    }

    private Date acquireDelayData(final int delayTime) {
        return new Date(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - (delayTime * 1000));
    }
}
