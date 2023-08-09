package org.dromara.hmily.repository.mongodb;

import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyMongoConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyLock;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.serializer.spi.exception.HmilySerializerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * @Author zhangzhi
 * @Date: 2023/8/9 13:33
 */
public class MongodbRepositoryTest {

    private MongodbRepository mongodbRepository;

    private final Random random = new Random(System.currentTimeMillis());
    private final List<HmilyLock> locks = new ArrayList<>();

    @Before
    public void setup() {
        Long transId = (long) random.nextInt(1000);
        Long participantId = (long) random.nextInt(1000);
        String resourceId = "jdbc:mysql://localhost:3306/test";
        for (int i = 1; i <= 5; i++) {
            HmilyLock lock = new HmilyLock(transId, participantId, resourceId, "tableName" + i, i + "");
            locks.add(lock);
        }
        registerConfig();
        mongodbRepository = new MongodbRepository();
        mongodbRepository.setSerializer(new HmilySerializer() {
            @Override
            public byte[] serialize(final Object obj) throws HmilySerializerException {
                try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream(); ObjectOutput objectOutput = new ObjectOutputStream(arrayOutputStream)) {
                    objectOutput.writeObject(obj);
                    objectOutput.flush();
                    return arrayOutputStream.toByteArray();
                } catch (IOException e) {
                    throw new HmilySerializerException("java serialize error " + e.getMessage());
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T deSerialize(final byte[] param, final Class<T> clazz) throws HmilySerializerException {
                try (ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(param); ObjectInput input = new ObjectInputStream(arrayInputStream)) {
                    return (T) input.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new HmilySerializerException("java deSerialize error " + e.getMessage());
                }
            }
        });
        mongodbRepository.init("appName");
    }

    @Test
    public void testWriteHmilyLocks() {
        int rows = mongodbRepository.writeHmilyLocks(locks);
        Assert.assertEquals(locks.size(), rows);
    }

    @Test
    public void testReleaseHmilyLocks() {
        int rows = mongodbRepository.releaseHmilyLocks(locks);
        Assert.assertEquals(locks.size(), rows);
    }

    @Test
    public void testFindHmilyLockById() {
        mongodbRepository.writeHmilyLocks(locks);
        // 锁不存在
        mongodbRepository.releaseHmilyLocks(locks);
        String lockId = locks.get(0).getLockId();
        Optional<HmilyLock> lockOptional = mongodbRepository.findHmilyLockById(lockId);
        Assert.assertEquals(Optional.empty(), lockOptional);

        // 锁存在
        mongodbRepository.writeHmilyLocks(locks);
        lockId = locks.get(0).getLockId();
        lockOptional = mongodbRepository.findHmilyLockById(lockId);
        Assert.assertEquals(lockId, lockOptional.get().getLockId());
    }

    private void registerConfig() {
        HmilyMongoConfig mongoConfig = new HmilyMongoConfig();
        mongoConfig.setUrl("localhost:27017");
        mongoConfig.setDatabaseName("hmily");
        mongoConfig.setUserName("test");
        mongoConfig.setPassword("test");
        ConfigEnv.getInstance().registerConfig(mongoConfig);
    }
}
