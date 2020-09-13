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

package org.dromara.hmily.repository.zookeeper;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyZookeeperConfig;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.zookeeper.mock.ZookeeperMock;
import org.dromara.hmily.serializer.kryo.KryoSerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Author:   lilang
 * Description: zookeeper repository test crud
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ZookeeperRepository.class})
@PowerMockIgnore({"javax.management.*", "com.intellij.*", "com.codahale.metrics.*"})
public class ZookeeperRepositoryTest {

    private static final String HMILY_TRANSACTION_GLOBAL = "hmily_transaction_global";

    private static final String HMILY_TRANSACTION_PRTICIPANT = "hmily_transaction_participant";

    private static final String HMILY_PARTICIPANT_UNDO = "hmily_participant_undo";

    // when it is false. please delete the annotation (@RunWith, @PrepareForTest, @PowerMockIgnore) of this class
    private boolean mockSwitch = true;
    
    private ZookeeperRepository zookeeperRepository = new ZookeeperRepository();
    
    private HmilyZookeeperConfig hmilyZookeeperConfig = new HmilyZookeeperConfig();
    
    private String appName = "test_hmily_zookeeper";
    
    private Random random = new Random(System.currentTimeMillis());

    /**
     * Sets up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        hmilyZookeeperConfig.setHost("127.0.0.1:2181");
        hmilyZookeeperConfig.setSessionTimeOut(300000);
        ConfigEnv.getInstance().registerConfig(hmilyZookeeperConfig);

        if (mockSwitch) {
            MockitoAnnotations.initMocks(this);
            ZookeeperMock zookeeperMock = new ZookeeperMock();
            Field latchFiled = ZookeeperRepository.class.getDeclaredField("LATCH");
            latchFiled.setAccessible(true);
            CountDownLatch latch = (CountDownLatch) latchFiled.get(null);
            PowerMockito.whenNew(ZooKeeper.class).withAnyArguments().then(x -> {
                latch.countDown();
                return zookeeperMock.getZooKeeper();
            });
            zookeeperMock.mockGetData();
            zookeeperMock.mockCreate();
            zookeeperMock.mockExists();
            zookeeperMock.mockGetChildren();
            zookeeperMock.mockSetData();
            zookeeperMock.mockDelete();
        }


        zookeeperRepository.init(appName);
        zookeeperRepository.setSerializer(new KryoSerializer());
        
        
        Field zooKeeperFiled = ZookeeperRepository.class.getDeclaredField("zooKeeper");
        zooKeeperFiled.setAccessible(true);
        ZooKeeper zooKeeper = (ZooKeeper) zooKeeperFiled.get(null);
        Stat stat = zooKeeper.exists("/hmily/" + appName + "/" + HMILY_TRANSACTION_GLOBAL, false);
        if (stat != null) {
            zooKeeper.delete("/hmily/" + appName + "/" + HMILY_TRANSACTION_GLOBAL, stat.getVersion());
        }
        
        stat = zooKeeper.exists("/hmily/" + appName + "/" + HMILY_TRANSACTION_PRTICIPANT, false);
        if (stat != null) {
            zooKeeper.delete("/hmily/" + appName + "/" + HMILY_TRANSACTION_PRTICIPANT, stat.getVersion());
        }
        
        stat = zooKeeper.exists("/hmily/" + appName + "/" + HMILY_PARTICIPANT_UNDO, false);
        if (stat != null) {
            zooKeeper.delete("/hmily/" + appName + "/" + HMILY_PARTICIPANT_UNDO, stat.getVersion());
        }
    }
    
    /**
     * Test curd.
     */
    @Test
    public void testCURD() {
        Long transactionId = (long) random.nextInt(1000);
        testTransaction(transactionId);
        
        Long participantId = (long) random.nextInt(1000);
        testParticipant(transactionId, participantId);
        
        Long undoId = (long) random.nextInt(1000);
        testParticipantUndo(transactionId, participantId, undoId);
    }
    
    private void testTransaction(Long transactionId) {
        HmilyTransaction hmilyTransaction = buildHmilyTransaction(transactionId);
        int result = zookeeperRepository.createHmilyTransaction(hmilyTransaction);
        assertNotEquals(0L, result);
        
        int updateStatusResult = zookeeperRepository.updateHmilyTransactionStatus(hmilyTransaction.getTransId(), 3);
        assertNotEquals(0L, updateStatusResult);
        
        hmilyTransaction.setTransType(TransTypeEnum.TAC.name());
        hmilyTransaction.setStatus(4);
        int updateLockResult = zookeeperRepository.updateRetryByLock(hmilyTransaction);
        assertNotEquals(0L, updateLockResult);
        
        
        
        HmilyTransaction findTransactionResult = zookeeperRepository.findByTransId(hmilyTransaction.getTransId());
        assertNotNull(findTransactionResult);
        assertEquals(appName, findTransactionResult.getAppName());
        assertEquals(TransTypeEnum.TAC.name(), findTransactionResult.getTransType());
        assertEquals(4L, findTransactionResult.getStatus());
        
        
        hmilyTransaction = buildHmilyTransaction((long) random.nextInt(1000));
        result = zookeeperRepository.createHmilyTransaction(hmilyTransaction);
        assertEquals(1L, result);
        
        hmilyTransaction = buildHmilyTransaction((long) random.nextInt(1000));
        result = zookeeperRepository.createHmilyTransaction(hmilyTransaction);
        assertEquals(1L, result);
        
        hmilyTransaction = buildHmilyTransaction((long) random.nextInt(1000));
        result = zookeeperRepository.createHmilyTransaction(hmilyTransaction);
        assertEquals(1L, result);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        List<HmilyTransaction> listTransactionResult = zookeeperRepository.listLimitByDelay(calendar.getTime(), 2);
        assertNotNull(listTransactionResult);
        assertNotEquals(0L, listTransactionResult.size());
        assertEquals(2L, listTransactionResult.size());
        int removeByIdResult = zookeeperRepository.removeHmilyTransaction(transactionId);
        assertEquals(1L, removeByIdResult);
        
        int removeByDateResult = zookeeperRepository.removeHmilyTransactionByData(calendar.getTime());
        assertEquals(3L, removeByDateResult);
    }
    
    private void testParticipantUndo(Long transactionId, Long participantId, Long undoId) {
        int result;
        HmilyParticipantUndo hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, undoId);
        result = zookeeperRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, result);
        
        
        int updateStatusResult = zookeeperRepository.updateHmilyParticipantUndoStatus(undoId, 3);
        assertEquals(1L, updateStatusResult);
        
        List<HmilyParticipantUndo> findUndoResult = zookeeperRepository.findHmilyParticipantUndoByParticipantId(hmilyParticipantUndo.getParticipantId());
        assertNotNull(findUndoResult);
        assertNotEquals(0L, findUndoResult.size());
        assertEquals(findUndoResult.get(0).getUndoId(), hmilyParticipantUndo.getUndoId());
        assertEquals(3L, (long) findUndoResult.get(0).getStatus());
        
        
        int removeByIdResult = zookeeperRepository.removeHmilyParticipantUndo(undoId);
        assertEquals(1L, removeByIdResult);
        
        hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, (long) random.nextInt(1000));
        result = zookeeperRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, result);
        hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, (long) random.nextInt(1000));
        result = zookeeperRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, result);
        hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, (long) random.nextInt(1000));
        result = zookeeperRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, result);
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        int removeByDateResult = zookeeperRepository.removeHmilyParticipantUndoByData(calendar.getTime());
        assertEquals(3L, removeByDateResult);
    }
    
    private void testParticipant(Long transactionId, Long participantId) {
        HmilyParticipant hmilyParticipant = buildHmilyParticipant(transactionId, participantId);
        int result = zookeeperRepository.createHmilyParticipant(hmilyParticipant);
        assertNotEquals(0L, result);
        
        
        int updateStatusResult = zookeeperRepository.updateHmilyParticipantStatus(participantId, 5);
        assertEquals(1L, updateStatusResult);
        hmilyParticipant.setStatus(4);
        boolean lockUpdateResult = zookeeperRepository.lockHmilyParticipant(hmilyParticipant);
        assertTrue(lockUpdateResult);
        
        List<HmilyParticipant> findParticipantResult = zookeeperRepository.findHmilyParticipant(hmilyParticipant.getParticipantId());
        assertNotNull(findParticipantResult);
        assertNotEquals(0L, findParticipantResult.size());
        assertTrue(findParticipantResult.get(0).getParticipantId().equals(hmilyParticipant.getParticipantId()) ||
                findParticipantResult.get(0).getParticipantRefId().equals(hmilyParticipant.getParticipantId()));
        assertEquals(4L, (long) findParticipantResult.stream()
                .filter(x -> x.getParticipantId()
                        .equals(participantId)).findFirst().get().getStatus());
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        long id1 = random.nextInt(1000);
        hmilyParticipant = buildHmilyParticipant(transactionId, id1);
        result = zookeeperRepository.createHmilyParticipant(hmilyParticipant);
        assertEquals(1L, result);
        long id2 = random.nextInt(1000);
        hmilyParticipant = buildHmilyParticipant(transactionId, id2);
        result = zookeeperRepository.createHmilyParticipant(hmilyParticipant);
        assertEquals(1L, result);
        long id3 = random.nextInt(1000);
        hmilyParticipant = buildHmilyParticipant(transactionId, id3);
        result = zookeeperRepository.createHmilyParticipant(hmilyParticipant);
        assertEquals(1L, result);
        List<HmilyParticipant> listResult = zookeeperRepository.listHmilyParticipant(calendar.getTime(), TransTypeEnum.TCC.name(), 2);
        assertEquals(2L, listResult.size());
        
        List<HmilyParticipant> listByTransactionIdResult = zookeeperRepository.listHmilyParticipantByTransId(transactionId);
        assertEquals(4L, listByTransactionIdResult.size());
        
        boolean existsRsult = zookeeperRepository.existHmilyParticipantByTransId(transactionId);
        assertTrue(existsRsult);
        
        int removeByIdResult = zookeeperRepository.removeHmilyParticipant(participantId);
        assertEquals(1L, removeByIdResult);
        
        zookeeperRepository.updateHmilyParticipantStatus(id1, 4);
        zookeeperRepository.updateHmilyParticipantStatus(id2, 4);
        zookeeperRepository.updateHmilyParticipantStatus(id3, 4);
        int removeByDateResult = zookeeperRepository.removeHmilyParticipantByData(calendar.getTime());
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
}
