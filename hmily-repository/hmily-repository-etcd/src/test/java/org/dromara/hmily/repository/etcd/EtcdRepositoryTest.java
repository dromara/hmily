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

package org.dromara.hmily.repository.etcd;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyEtcdConfig;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.serializer.kryo.KryoSerializer;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * etcd repository test.
 *
 * @author dongzl
 */
@RunWith(MockitoJUnitRunner.class)
public class EtcdRepositoryTest {
    
    private EtcdRepository etcdRepository = new EtcdRepository();
    
    private HmilyEtcdConfig hmilyEtcdConfig = new HmilyEtcdConfig();

    private HmilySerializer hmilySerializer;
    
    private String appName = "test_hmily_etcd";
    
    private Random random = new Random(System.currentTimeMillis());

    @Mock
    private Client client;

    @Mock
    private KV kv;

    @Mock
    private CompletableFuture getFuture;

    @Mock
    private GetResponse getResponse;

    @Mock
    private CompletableFuture putFuture;
    
    @Mock
    private CompletableFuture deleteFuture;
    
    @Mock
    private DeleteResponse deleteResponse;
    
    @Mock
    private KeyValue keyValue;

    /**
     * Sets up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        hmilyEtcdConfig.setHost("127.0.0.1:2379");
        ConfigEnv.getInstance().registerConfig(hmilyEtcdConfig);
        hmilySerializer = new KryoSerializer();
        etcdRepository.setSerializer(hmilySerializer);
        when(client.getKVClient()).thenReturn(kv);
        when(kv.get(any(ByteSequence.class))).thenReturn(getFuture);
        when(kv.get(any(ByteSequence.class), any(GetOption.class))).thenReturn(getFuture);
        when(kv.put(any(ByteSequence.class), any(ByteSequence.class))).thenReturn(putFuture);
        when(kv.delete(any(ByteSequence.class))).thenReturn(deleteFuture);
        when(getFuture.get()).thenReturn(getResponse);
        when(getResponse.getKvs()).thenReturn(Lists.newArrayList(keyValue));
        when(deleteFuture.get()).thenReturn(deleteResponse);
        when(getResponse.getCount()).thenReturn(0L);
        FieldSetter.setField(etcdRepository, etcdRepository.getClass().getDeclaredField("client"), client);
        FieldSetter.setField(etcdRepository, etcdRepository.getClass().getDeclaredField("appName"), appName);
    }
    
    @Test
    public void testCreateHmilyTransaction() {
        Long transactionId = (long) random.nextInt(1000);
        HmilyTransaction hmilyTransaction = buildHmilyTransaction(transactionId);
        int result = etcdRepository.createHmilyTransaction(hmilyTransaction);
        assertEquals(1L, result);
    }
    
    @Test
    public void testUpdateRetryByLock() {
        Long transactionId = (long) random.nextInt(1000);
        HmilyTransaction hmilyTransaction = buildHmilyTransaction(transactionId);
        hmilyTransaction.setTransType(TransTypeEnum.TAC.name());
        hmilyTransaction.setStatus(4);
        int updateLockResult = etcdRepository.updateRetryByLock(hmilyTransaction);
        assertNotEquals(0L, updateLockResult);
    }
    
    @Test
    public void testFindByTransId() {
        long transactionId = random.nextInt(1000);
        HmilyTransaction hmilyTransaction = buildHmilyTransaction(transactionId);
        hmilyTransaction.setTransType(TransTypeEnum.TAC.name());
        hmilyTransaction.setStatus(4);
        when(keyValue.getValue()).thenReturn(ByteSequence.from(hmilySerializer.serialize(hmilyTransaction)));
        HmilyTransaction findTransactionResult = etcdRepository.findByTransId(hmilyTransaction.getTransId());
        assertNotNull(findTransactionResult);
        assertEquals(appName, findTransactionResult.getAppName());
        assertEquals(TransTypeEnum.TAC.name(), findTransactionResult.getTransType());
        assertEquals(4L, findTransactionResult.getStatus());
    }
    
    @Test
    public void testListLimitByDelay() {
        HmilyTransaction hmilyTransaction1 = buildHmilyTransaction((long) random.nextInt(1000));
        HmilyTransaction hmilyTransaction2 = buildHmilyTransaction((long) random.nextInt(1000));
        io.etcd.jetcd.api.KeyValue keyValue1 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/hmily_transaction_global/" + hmilyTransaction1.getTransId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyTransaction1))).build();
        io.etcd.jetcd.api.KeyValue keyValue2 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/hmily_transaction_global/" + + hmilyTransaction1.getTransId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyTransaction2))).build();
        List<KeyValue> keyValues = Arrays.asList(new KeyValue(keyValue1, ByteSequence.EMPTY), new KeyValue(keyValue2, ByteSequence.EMPTY),
                new KeyValue(keyValue1, ByteSequence.EMPTY));
        when(getResponse.getKvs()).thenReturn(keyValues);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        List<HmilyTransaction> listTransactionResult = etcdRepository.listLimitByDelay(calendar.getTime(), 2);
        assertNotNull(listTransactionResult);
        assertNotEquals(0L, listTransactionResult.size());
        assertEquals(2L, listTransactionResult.size());
    }
    
    @Test
    public void testUpdateHmilyTransactionStatus() {
        long transactionId = random.nextInt(1000);
        HmilyTransaction hmilyTransaction = buildHmilyTransaction(transactionId);
        when(keyValue.getValue()).thenReturn(ByteSequence.from(hmilySerializer.serialize(hmilyTransaction)));
        int updateStatusResult = etcdRepository.updateHmilyTransactionStatus(hmilyTransaction.getTransId(), 3);
        assertEquals(1L, updateStatusResult);
    }
    
    @Test
    public void testRemoveHmilyTransaction() {
        long transactionId = random.nextInt(1000);
        int removeByIdResult = etcdRepository.removeHmilyTransaction(transactionId);
        assertEquals(1L, removeByIdResult);
    }
    
    @Test
    public void testRemoveHmilyTransactionByDate() {
        HmilyTransaction hmilyTransaction1 = buildHmilyTransaction((long) random.nextInt(1000));
        hmilyTransaction1.setStatus(1);
        io.etcd.jetcd.api.KeyValue keyValue1 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/hmily_transaction_global/" + hmilyTransaction1.getTransId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyTransaction1))).build();

        HmilyTransaction hmilyTransaction2 = buildHmilyTransaction((long) random.nextInt(1000));
        io.etcd.jetcd.api.KeyValue keyValue2 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/hmily_transaction_global/" + + hmilyTransaction2.getTransId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyTransaction2))).build();
        
        List<KeyValue> keyValues = Arrays.asList(new KeyValue(keyValue1, ByteSequence.EMPTY), new KeyValue(keyValue2, ByteSequence.EMPTY));
        when(getResponse.getKvs()).thenReturn(keyValues);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        int removeByDateResult = etcdRepository.removeHmilyTransactionByDate(new Date());
        assertEquals(1L, removeByDateResult);
    }
    
    @Test
    public void testCreateHmilyParticipant() {
        long transactionId = random.nextInt(1000);
        long participantId = random.nextInt(1000);
        HmilyParticipant hmilyParticipant = buildHmilyParticipant(transactionId, participantId);
        int result = etcdRepository.createHmilyParticipant(hmilyParticipant);
        assertEquals(1L, result);
    }
    
    @Test
    public void testFindHmilyParticipant() {
        HmilyParticipant hmilyParticipant = buildHmilyParticipant((long) random.nextInt(1000), (long) random.nextInt(1000));
        io.etcd.jetcd.api.KeyValue kv1 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/test_hmily_etcd/hmily_transaction_participant/" + hmilyParticipant.getTransId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyParticipant))).build();

        List<KeyValue> keyValues = Arrays.asList(new KeyValue(kv1, ByteSequence.EMPTY));
        when(getResponse.getKvs()).thenReturn(keyValues);
        List<HmilyParticipant> findParticipantResult = etcdRepository.findHmilyParticipant(hmilyParticipant.getParticipantId());
        assertNotNull(findParticipantResult);
        assertNotEquals(0L, findParticipantResult.size());
        assertTrue(findParticipantResult.get(0).getParticipantId().equals(hmilyParticipant.getParticipantId()) ||
                findParticipantResult.get(0).getParticipantRefId().equals(hmilyParticipant.getParticipantId()));
        assertEquals(3L, (long) findParticipantResult.stream()
                .filter(x -> x.getParticipantId()
                        .equals(hmilyParticipant.getParticipantId())).findFirst().get().getStatus());
    }

    @Test
    public void testListHmilyParticipant() {
        long transactionId = random.nextInt(1000);
        HmilyParticipant hmilyParticipant1 = buildHmilyParticipant(transactionId, (long) random.nextInt(1000));
        io.etcd.jetcd.api.KeyValue kv1 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/test_hmily_etcd/hmily_transaction_participant/" + hmilyParticipant1.getTransId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyParticipant1))).build();
        
        HmilyParticipant hmilyParticipant2 = buildHmilyParticipant(transactionId, (long) random.nextInt(1000));
        io.etcd.jetcd.api.KeyValue kv2 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/test_hmily_etcd/hmily_transaction_participant/" + hmilyParticipant2.getTransId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyParticipant2))).build();
        
        HmilyParticipant hmilyParticipant3 = buildHmilyParticipant(transactionId, (long) random.nextInt(1000));
        io.etcd.jetcd.api.KeyValue kv3 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/test_hmily_etcd/hmily_transaction_participant/" + hmilyParticipant3.getTransId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyParticipant3))).build();
        
        List<KeyValue> keyValues = Arrays.asList(new KeyValue(kv1, ByteSequence.EMPTY), new KeyValue(kv2, ByteSequence.EMPTY), 
                new KeyValue(kv3, ByteSequence.EMPTY));
        when(getResponse.getKvs()).thenReturn(keyValues);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        List<HmilyParticipant> listResult = etcdRepository.listHmilyParticipant(calendar.getTime(), TransTypeEnum.TCC.name(), 2);
        assertEquals(2L, listResult.size());
    }

    @Test
    public void testListHmilyParticipantByTransId() {
        long transId = random.nextInt(1000);
        HmilyParticipant hmilyParticipant1 = buildHmilyParticipant(transId, (long) random.nextInt(1000));
        io.etcd.jetcd.api.KeyValue kv1 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/test_hmily_etcd/hmily_transaction_participant/" + hmilyParticipant1.getTransId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyParticipant1))).build();
        
        HmilyParticipant hmilyParticipant2 = buildHmilyParticipant(transId, (long) random.nextInt(1000));
        io.etcd.jetcd.api.KeyValue kv2 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/test_hmily_etcd/hmily_transaction_participant/" + hmilyParticipant2.getTransId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyParticipant2))).build();

        List<KeyValue> keyValues = Arrays.asList(new KeyValue(kv1, ByteSequence.EMPTY), new KeyValue(kv2, ByteSequence.EMPTY));
        when(getResponse.getKvs()).thenReturn(keyValues);
        List<HmilyParticipant> listByTransactionIdResult = etcdRepository.listHmilyParticipantByTransId(transId);
        assertEquals(2L, listByTransactionIdResult.size());
    }
    
    @Test
    public void testExistHmilyParticipantByTransId() {
        HmilyParticipant hmilyParticipant = buildHmilyParticipant((long) random.nextInt(1000), (long) random.nextInt(1000));
        when(keyValue.getValue()).thenReturn(ByteSequence.from(hmilySerializer.serialize(hmilyParticipant)));
        boolean existsRsult = etcdRepository.existHmilyParticipantByTransId(hmilyParticipant.getTransId());
        assertTrue(existsRsult);
    }

    @Test
    public void testUpdateHmilyParticipantStatus() {
        HmilyParticipant hmilyParticipant = buildHmilyParticipant((long) random.nextInt(1000), (long) random.nextInt(1000));
        when(keyValue.getValue()).thenReturn(ByteSequence.from(hmilySerializer.serialize(hmilyParticipant)));
        int updateStatusResult = etcdRepository.updateHmilyParticipantStatus(hmilyParticipant.getParticipantId(), 5);
        assertEquals(1L, updateStatusResult);
    }

    @Test
    public void testRemoveHmilyParticipant() {
        int removeByIdResult = etcdRepository.removeHmilyParticipant((long) random.nextInt(1000));
        assertEquals(1L, removeByIdResult);
    }

    @Test
    public void testRemoveHmilyParticipantByDate() {
        HmilyParticipant hmilyParticipant1 = buildHmilyParticipant((long) random.nextInt(1000),
                (long) random.nextInt(1000));
        hmilyParticipant1.setStatus(4);
        io.etcd.jetcd.api.KeyValue kv1 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/test_hmily_etcd/hmily_transaction_participant/" + hmilyParticipant1.getTransId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyParticipant1))).build();

        HmilyParticipant hmilyParticipant2 = buildHmilyParticipant((long) random.nextInt(1000),
                (long) random.nextInt(1000));
        io.etcd.jetcd.api.KeyValue kv2 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/test_hmily_etcd/hmily_transaction_participant/" + hmilyParticipant2.getTransId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyParticipant2))).build();
        
        List<KeyValue> keyValues = Arrays.asList(new KeyValue(kv1, ByteSequence.EMPTY), new KeyValue(kv2, ByteSequence.EMPTY));
        when(getResponse.getKvs()).thenReturn(keyValues);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        int removeByDateResult = etcdRepository.removeHmilyParticipantByDate(calendar.getTime());
        assertEquals(1L, removeByDateResult);
    }

    @Test
    public void testLockHmilyParticipant() {
        HmilyParticipant hmilyParticipant = buildHmilyParticipant((long) random.nextInt(1000), 
                (long) random.nextInt(1000));
        when(keyValue.getVersion()).thenReturn((long) hmilyParticipant.getVersion());
        hmilyParticipant.setStatus(4);
        boolean lockUpdateResult = etcdRepository.lockHmilyParticipant(hmilyParticipant);
        assertTrue(lockUpdateResult);
    }

    @Test
    public void testCreateHmilyParticipantUndo() {
        Long transactionId = (long) random.nextInt(1000);
        Long participantId = (long) random.nextInt(1000);
        Long undoId = (long) random.nextInt(1000);
        HmilyParticipantUndo hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, undoId);
        int result = etcdRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, result);
    }

    @Test
    public void testFindHmilyParticipantUndoByParticipantId() {
        Long transactionId = (long) random.nextInt(1000);
        Long participantId = (long) random.nextInt(1000);
        Long undoId = (long) random.nextInt(1000);
        HmilyParticipantUndo hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, undoId);
        io.etcd.jetcd.api.KeyValue kv = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/test_hmily_etcd/hmily_participant_undo"))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyParticipantUndo))).build();
        when(getResponse.getKvs()).thenReturn(Lists.newArrayList(new KeyValue(kv, ByteSequence.EMPTY)));
        List<HmilyParticipantUndo> findUndoResult = etcdRepository.findHmilyParticipantUndoByParticipantId(participantId);
        assertNotNull(findUndoResult);
        assertNotEquals(0L, findUndoResult.size());
        assertEquals(findUndoResult.get(0).getUndoId(), hmilyParticipantUndo.getUndoId());
        assertEquals(4L, (long) findUndoResult.get(0).getStatus());
    }

    @Test
    public void testRemoveHmilyParticipantUndo() {
        int removeByIdResult = etcdRepository.removeHmilyParticipantUndo((long) random.nextInt(1000));
        assertEquals(1L, removeByIdResult);
    }

    @Test
    public void testRemoveHmilyParticipantUndoByDate() {
        HmilyParticipantUndo hmilyParticipantUndo1 = buildHmilyParticipantUndo((long) random.nextInt(1000), 
                (long) random.nextInt(1000), (long) random.nextInt(1000));
        hmilyParticipantUndo1.setStatus(1);
        io.etcd.jetcd.api.KeyValue kv1 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/test_hmily_etcd/hmily_participant_undo/" + hmilyParticipantUndo1.getUndoId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyParticipantUndo1))).build();
        
        HmilyParticipantUndo hmilyParticipantUndo2 = buildHmilyParticipantUndo((long) random.nextInt(1000),
                (long) random.nextInt(1000), (long) random.nextInt(1000));
        io.etcd.jetcd.api.KeyValue kv2 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("/hmily-repository/test_hmily_etcd/hmily_participant_undo/" + hmilyParticipantUndo2.getUndoId()))
                .setValue(ByteString.copyFrom(hmilySerializer.serialize(hmilyParticipantUndo2))).build();
        
        List<KeyValue> keyValues = Arrays.asList(new KeyValue(kv1, ByteSequence.EMPTY), new KeyValue(kv2, ByteSequence.EMPTY));
        when(getResponse.getKvs()).thenReturn(keyValues);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        int removeByDateResult = etcdRepository.removeHmilyParticipantUndoByDate(calendar.getTime());
        assertEquals(1L, removeByDateResult);
    }

    @Test
    public void testUpdateHmilyParticipantUndoStatus() {
        HmilyParticipantUndo hmilyParticipantUndo = buildHmilyParticipantUndo((long) random.nextInt(1000),
                (long) random.nextInt(1000), (long) random.nextInt(1000));
        when(keyValue.getValue()).thenReturn(ByteSequence.from(hmilySerializer.serialize(hmilyParticipantUndo)));
        int updateStatusResult = etcdRepository.updateHmilyParticipantUndoStatus(hmilyParticipantUndo.getUndoId(), 3);
        assertEquals(1L, updateStatusResult);
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
