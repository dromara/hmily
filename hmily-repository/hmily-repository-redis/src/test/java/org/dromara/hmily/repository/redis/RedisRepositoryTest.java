package org.dromara.hmily.repository.redis;

import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyRedisConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyLock;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.serializer.spi.exception.HmilySerializerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

/**
 * @Author zhangzhi
 * @Date: 2023/7/7 10:40
 */
public class RedisRepositoryTest {

    private RedisRepository redisRepository;

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
        redisRepository = new RedisRepository();
        redisRepository.setSerializer(new HmilySerializer() {
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
        redisRepository.init("test");
    }

    @Test
    public void testWriteHmilyLocks() {
        int rows = redisRepository.writeHmilyLocks(locks);
        Assert.assertEquals(locks.size(), rows);
    }

    @Test
    public void testReleaseHmilyLocks() {
        int rows = redisRepository.releaseHmilyLocks(locks);
        Assert.assertEquals(HmilyRepository.ROWS, rows);
    }

    @Test
    public void testFindHmilyLockById() {
        redisRepository.writeHmilyLocks(locks);
        // 锁不存在
        redisRepository.releaseHmilyLocks(locks);
        String lockId = locks.get(0).getLockId();
        Optional<HmilyLock> lockOptional = redisRepository.findHmilyLockById(lockId);
        Assert.assertEquals(Optional.empty(), lockOptional);

        // 锁存在
        redisRepository.writeHmilyLocks(locks);
        lockId = locks.get(0).getLockId();
        lockOptional = redisRepository.findHmilyLockById(lockId);
        Assert.assertEquals(lockId, lockOptional.get().getLockId());
    }

    private void registerConfig() {
        HmilyRedisConfig redisConfig = new HmilyRedisConfig();
        redisConfig.setHostName("localhost");
        redisConfig.setPort(6379);
        ConfigEnv.getInstance().registerConfig(redisConfig);
    }
}
