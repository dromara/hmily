package org.dromara.hmily.repository.zookeeper;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.config.HmilyConfig;
import org.dromara.hmily.config.HmilyZookeeperConfig;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.serializer.kryo.KryoSerializer;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
/**
 * Author:   lilang
 * Date:     2020-08-16 10:58
 * Description: zookeeper repository test crud
 **/
public class ZookeeperRepositoryTest {

    private ZookeeperRepository zookeeperRepository = new ZookeeperRepository();

    private HmilyConfig hmilyConfig = new HmilyConfig();
    private HmilyZookeeperConfig hmilyZookeeperConfig = new HmilyZookeeperConfig();

    private String appName = "test_hmily_zookeeper";
    private Random random = new Random(System.currentTimeMillis());

    private static final String HMILY_TRANSACTION_GLOBAL = "hmily_transaction_global";

    private static final String HMILY_TRANSACTION_PRTICIPANT = "hmily_transaction_participant";

    private static final String HMILY_PARTICIPANT_UNDO = "hmily_participant_undo";

    @Before
    public void setUp() throws Exception {
        hmilyConfig.setAppName(appName);
        hmilyConfig.setHmilyZookeeperConfig(hmilyZookeeperConfig);
        hmilyZookeeperConfig.setHost("127.0.0.1:2181");
        hmilyZookeeperConfig.setSessionTimeOut(300000);

        zookeeperRepository.init(hmilyConfig);
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

    @Test
    public void testCURD() {
        Long transactionId = Long.valueOf(random.nextInt(1000));
        testTransaction(transactionId);

        Long participantId = Long.valueOf(random.nextInt(1000));
        testParticipant(transactionId, participantId);

        Long undoId = Long.valueOf(random.nextInt(1000));
        testParticipantUndo(transactionId, participantId, undoId);
    }

    private void testTransaction(Long transactionId) {
        HmilyTransaction hmilyTransaction = buildHmilyTransaction(transactionId);
        int result = zookeeperRepository.createHmilyTransaction(hmilyTransaction);
        assertNotEquals(0L, (long) result);

        int updateStatusResult = zookeeperRepository.updateHmilyTransactionStatus(hmilyTransaction.getTransId(), 3);
        assertNotEquals(0L, (long) updateStatusResult);

        hmilyTransaction.setTransType(TransTypeEnum.TAC.name());
        hmilyTransaction.setStatus(4);
        int updateLockResult = zookeeperRepository.updateRetryByLock(hmilyTransaction);
        assertNotEquals(0L, (long) updateLockResult);



        HmilyTransaction findTransactionResult = zookeeperRepository.findByTransId(hmilyTransaction.getTransId());
        assertNotNull(findTransactionResult);
        assertEquals(appName, findTransactionResult.getAppName());
        assertEquals(TransTypeEnum.TAC.name(), findTransactionResult.getTransType());
        assertEquals(4L, findTransactionResult.getStatus());


        hmilyTransaction = buildHmilyTransaction((long) random.nextInt(1000));
        result = zookeeperRepository.createHmilyTransaction(hmilyTransaction);
        assertEquals(1L, (long) result);

        hmilyTransaction = buildHmilyTransaction((long) random.nextInt(1000));
        result = zookeeperRepository.createHmilyTransaction(hmilyTransaction);
        assertEquals(1L, (long) result);

        hmilyTransaction = buildHmilyTransaction((long) random.nextInt(1000));
        result = zookeeperRepository.createHmilyTransaction(hmilyTransaction);
        assertEquals(1L, (long) result);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        List<HmilyTransaction> listTransactionResult = zookeeperRepository.listLimitByDelay(calendar.getTime(), 2);
        assertNotNull(listTransactionResult);
        assertNotEquals(0L, listTransactionResult.size());
        assertEquals(2L, (long) listTransactionResult.size());
        int removeByIdResult = zookeeperRepository.removeHmilyTransaction(transactionId);
        assertEquals(1L, (long) removeByIdResult);

        int removeByDateResult = zookeeperRepository.removeHmilyTransactionByData(calendar.getTime());
        assertEquals(3L, (long) removeByDateResult);
    }

    private void testParticipantUndo(Long transactionId, Long participantId, Long undoId) {
        int result;
        HmilyParticipantUndo hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, undoId);
        result = zookeeperRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, (long) result);


        int updateStatusResult = zookeeperRepository.updateHmilyParticipantUndoStatus(undoId, 3);
        assertEquals(1L, (long) updateStatusResult);

        List<HmilyParticipantUndo> findUndoResult = zookeeperRepository.findHmilyParticipantUndoByParticipantId(hmilyParticipantUndo.getParticipantId());
        assertNotNull(findUndoResult);
        assertNotEquals(0L, (long) findUndoResult.size());
        assertEquals(findUndoResult.get(0).getUndoId(), hmilyParticipantUndo.getUndoId());
        assertEquals(3L, (long) findUndoResult.get(0).getStatus());


        int removeByIdResult = zookeeperRepository.removeHmilyParticipantUndo(undoId);
        assertEquals(1L, (long) removeByIdResult);

        hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, (long) random.nextInt(1000));
        result = zookeeperRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, (long) result);
        hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, (long) random.nextInt(1000));
        result = zookeeperRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, (long) result);
        hmilyParticipantUndo = buildHmilyParticipantUndo(transactionId, participantId, (long) random.nextInt(1000));
        result = zookeeperRepository.createHmilyParticipantUndo(hmilyParticipantUndo);
        assertNotEquals(0L, (long) result);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        int removeByDateResult = zookeeperRepository.removeHmilyParticipantUndoByData(calendar.getTime());
        assertEquals(3L, (long) removeByDateResult);
    }

    private void testParticipant(Long transactionId, Long participantId) {
        HmilyParticipant hmilyParticipant = buildHmilyParticipant(transactionId, participantId);
        int result = zookeeperRepository.createHmilyParticipant(hmilyParticipant);
        assertNotEquals(0L, (long) result);


        int updateStatusResult = zookeeperRepository.updateHmilyParticipantStatus(participantId, 5);
        assertEquals(1L, updateStatusResult);
        hmilyParticipant.setStatus(4);
        boolean lockUpdateResult = zookeeperRepository.lockHmilyParticipant(hmilyParticipant);
        assertTrue(lockUpdateResult);

        List<HmilyParticipant> findParticipantResult = zookeeperRepository.findHmilyParticipant(hmilyParticipant.getParticipantId());
        assertNotNull(findParticipantResult);
        assertNotEquals(0L, (long) findParticipantResult.size());
        assertTrue(findParticipantResult.get(0).getParticipantId().equals(hmilyParticipant.getParticipantId()) ||
                findParticipantResult.get(0).getParticipantRefId().equals(hmilyParticipant.getParticipantId()));
        assertEquals(4L, (long) findParticipantResult.stream()
                .filter(x -> x.getParticipantId()
                        .equals(participantId)).findFirst().get().getStatus());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
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
        assertEquals(1L, (long) removeByIdResult);

        zookeeperRepository.updateHmilyParticipantStatus(id1, 4);
        zookeeperRepository.updateHmilyParticipantStatus(id2, 4);
        zookeeperRepository.updateHmilyParticipantStatus(id3, 4);
        int removeByDateResult = zookeeperRepository.removeHmilyParticipantByData(calendar.getTime());
        assertEquals(3L, (long) removeByDateResult);
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
