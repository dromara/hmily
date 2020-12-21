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

import lombok.SneakyThrows;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.exception.HmilyException;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.utils.AssertUtils;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyFileConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyLock;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.serializer.spi.exception.HmilySerializerException;
import org.dromara.hmily.spi.HmilySPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * file impl.
 *
 * @author xiaoyu
 * @author choviwu
 */
@SuppressWarnings("all")
@HmilySPI("file")
public class FileRepository implements HmilyRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileRepository.class);

    private static final String HMILY_ROOT_TRANSACTION = "hmily";

    private static final String HMILY_TRANSATION_PARTICIPANT = "participant";

    private static final String HMILY_PARTICIPANT_UNDO = "undo";

    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    private static final int HMILY_READ_BYTE_SIZE = 2048;

    private static volatile boolean initialized;

    private HmilySerializer hmilySerializer;

    private String appName;

    private String filePath;

    @Override
    public void init(final String appName) {
        this.appName = appName;
        HmilyFileConfig fileConfig = ConfigEnv.getInstance().getConfig(HmilyFileConfig.class);
        filePath = StringUtils.isBlank(fileConfig.getPath()) ? System.getProperty("user.home") : fileConfig.getPath();
        Path workPath = Paths.get(filePath);
        AssertUtils.notNull(Files.isDirectory(workPath));
        makeDir();
    }

    @Override
    public void setSerializer(final HmilySerializer hmilySerializer) {
        this.hmilySerializer = hmilySerializer;
    }

    @SneakyThrows
    @Override
    public int createHmilyTransaction(final HmilyTransaction hmilyTransaction) throws HmilyRepositoryException {
        try {
            final boolean exsist = isExsist(getTransationPath(), hmilyTransaction.getTransId());
            if (!exsist) {
                hmilyTransaction.setCreateTime(new Date());
                hmilyTransaction.setUpdateTime(new Date());
                hmilyTransaction.setAppName(appName);
                createFile(getTransationPath(), HmilyTransaction.class, hmilyTransaction.getTransId(), hmilyTransaction);
            } else {
                String filePath = concatPath(getTransationPath(), hmilyTransaction.getTransId());
                writeTransactionFile(filePath, HmilyTransaction.class, hmilyTransaction);
            }
            return HmilyRepository.ROWS;
        } catch (IOException e) {
            throw new HmilyException(e);
        }
    }

    @Override
    public int updateRetryByLock(final HmilyTransaction hmilyTransaction) {
        final boolean exsist = isExsist(getTransationPath(), hmilyTransaction.getTransId());
        if (!exsist) {
            return HmilyRepository.FAIL_ROWS;
        }
        String filePath = concatPath(getTransationPath(), hmilyTransaction.getTransId());
        return writeTransactionFile(filePath, HmilyTransaction.class, hmilyTransaction);
    }

    @Override
    public HmilyTransaction findByTransId(final Long transId) {
        boolean exsist = isExsist(getTransationPath(), transId);
        if (exsist) {
            return readFile(getTransationPath(), HmilyTransaction.class, transId);
        }
        return null;
    }

    @Override
    public List<HmilyTransaction> listLimitByDelay(final Date date, final int limit) {
        return listByFilter(getTransationPath(), HmilyTransaction.class, (hmilyTransaction, params) -> {
            Date dateParam = (Date) params[0];
            int limitParam = (int) params[1];
            boolean filterResult = dateParam.after(hmilyTransaction.getUpdateTime())
                    && Objects.equals(appName, hmilyTransaction.getAppName())
                    && limitParam-- > 0;
            // write back to params
            params[1] = limitParam;
            return filterResult;
        }, date, limit);
    }

    @Override
    public int updateHmilyTransactionStatus(final Long transId, final Integer status) throws HmilyRepositoryException {
        boolean exsist = isExsist(getTransationPath(), transId);
        if (!exsist) {
            return HmilyRepository.FAIL_ROWS;
        }
        HmilyTransaction hmilyTransaction = readFile(getTransationPath(), HmilyTransaction.class, transId);
        hmilyTransaction.setStatus(status);
        String filePath = concatPath(getTransationPath(), transId);
        return writeTransactionFile(filePath, HmilyTransaction.class, hmilyTransaction);
    }

    @Override
    public int removeHmilyTransaction(final Long transId) {
        try {
            boolean exsist = isExsist(getTransationPath(), transId);
            if (!exsist) {
                return HmilyRepository.FAIL_ROWS;
            }
            HmilyTransaction hmilyTransaction = readFile(getTransationPath(), HmilyTransaction.class, transId);
            if (hmilyTransaction == null) {
                return HmilyRepository.FAIL_ROWS;
            }
            boolean delete = deleteFile(getTransationPath(), transId);
            return delete ? HmilyRepository.ROWS : HmilyRepository.FAIL_ROWS;
        } catch (IOException e) {
            LogUtil.error(LOGGER, "removeHmilyTransaction occur a exception {}", e::getMessage);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyTransactionByDate(final Date date) {
        return removeByFilter(getTransationPath(), HmilyTransaction.class, (hmilyTransaction, params) -> {
            Date dateParam = (Date) params[0];
            return dateParam.after(hmilyTransaction.getUpdateTime()) && hmilyTransaction.getStatus() == HmilyActionEnum.DELETE.getCode();
        }, date);
    }

    @Override
    public int createHmilyParticipant(final HmilyParticipant hmilyParticipant) throws HmilyRepositoryException {
        try {
            boolean exsist = isExsist(getParticipantPath(), hmilyParticipant.getParticipantId());
            if (!exsist) {
                hmilyParticipant.setCreateTime(new Date());
                hmilyParticipant.setUpdateTime(new Date());
                hmilyParticipant.setAppName(appName);
                createFile(getParticipantPath(), HmilyParticipant.class, hmilyParticipant.getParticipantId(), hmilyParticipant);
            } else {
                writeParticipantFile(getParticipantPath(), HmilyParticipant.class, hmilyParticipant.getParticipantId(), hmilyParticipant.getStatus(), 0);
            }
            return HmilyRepository.ROWS;
        } catch (IOException e) {
            throw new HmilyException(e);
        }
    }

    @Override
    public List<HmilyParticipant> findHmilyParticipant(final Long participantId) {
        return listByFilter(getParticipantPath(), HmilyParticipant.class, (hmilyParticipant, params) -> {
            Long participantIdParam = (Long) params[0];
            return participantIdParam.compareTo(hmilyParticipant.getParticipantId()) == 0
                    || (hmilyParticipant.getParticipantRefId() != null && participantIdParam.compareTo(hmilyParticipant.getParticipantRefId()) == 0);
        }, participantId);
    }

    @Override
    public List<HmilyParticipant> listHmilyParticipant(final Date date, final String transType, final int limit) {
        return listByFilter(getParticipantPath(), HmilyParticipant.class, (hmilyParticipant, params) -> {
            Date dateParam = (Date) params[0];
            String transTypeParam = (String) params[1];
            int limitParam = (int) params[2];
            boolean filterResult = dateParam.after(hmilyParticipant.getUpdateTime())
                    && Objects.equals(appName, hmilyParticipant.getAppName())
                    && Objects.equals(transTypeParam, hmilyParticipant.getTransType())
                    && (hmilyParticipant.getStatus().compareTo(HmilyActionEnum.DELETE.getCode()) != 0
                    && hmilyParticipant.getStatus().compareTo(HmilyActionEnum.DEATH.getCode()) != 0)
                    && limitParam-- > 0;
            params[2] = limitParam;
            return filterResult;
        }, date, transType, limit);
    }

    @Override
    public List<HmilyParticipant> listHmilyParticipantByTransId(final Long transId) {
        return listByFilter(getParticipantPath(), HmilyParticipant.class, (hmilyParticipant, params) -> transId.compareTo(hmilyParticipant.getTransId()) == 0, transId);
    }

    @Override
    public boolean existHmilyParticipantByTransId(final Long transId) {
        return existByFilter((hmilyParticipant, params) -> {
            Long transIdParam = (Long) params[0];
            return transIdParam.compareTo(hmilyParticipant.getTransId()) == 0;
        }, transId);
    }

    @Override
    public int updateHmilyParticipantStatus(final Long participantId, final Integer status) throws HmilyRepositoryException {
        boolean exsist = isExsist(getParticipantPath(), participantId);
        if (!exsist) {
            return HmilyRepository.FAIL_ROWS;
        }
        return writeParticipantFile(getParticipantPath(), HmilyParticipant.class, participantId, status, 0);
    }

    @Override
    public int removeHmilyParticipant(final Long participantId) {
        try {
            boolean exsist = isExsist(getParticipantPath(), participantId);
            if (!exsist) {
                return HmilyRepository.FAIL_ROWS;
            }
            HmilyParticipant hmilyParticipant = readFile(getParticipantPath(), HmilyParticipant.class, participantId);
            if (hmilyParticipant == null) {
                return HmilyRepository.FAIL_ROWS;
            }
            boolean delete = deleteFile(getParticipantPath(), participantId);
            return delete ? HmilyRepository.ROWS : HmilyRepository.FAIL_ROWS;
        } catch (IOException e) {
            LogUtil.error(LOGGER, "removeHmilyParticipant occur a exception {}", e::getMessage);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyParticipantByDate(final Date date) {
        return removeByFilter(getParticipantPath(), HmilyParticipant.class, (hmilyParticipant, params) -> {
            Date dateParam = (Date) params[0];
            return dateParam.after(hmilyParticipant.getUpdateTime()) && Objects.equals(HmilyActionEnum.DELETE.getCode(), hmilyParticipant.getStatus());
        }, date);
    }

    @Override
    public boolean lockHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        final int currentVersion = hmilyParticipant.getVersion();
        boolean exsist = isExsist(getParticipantPath(), hmilyParticipant.getParticipantId());
        if (!exsist) {
            LogUtil.warn(LOGGER, "path {} is not exists.", () -> getParticipantPath());
            return false;
        }
        writeParticipantFile(getParticipantPath(), HmilyParticipant.class, hmilyParticipant.getParticipantId(), hmilyParticipant.getStatus(), 1);
        return true;
    }

    @Override
    public int createHmilyParticipantUndo(final HmilyParticipantUndo hmilyParticipantUndo) {
        try {
            hmilyParticipantUndo.setCreateTime(new Date());
            hmilyParticipantUndo.setUpdateTime(new Date());
            createFile(getParticipantUndoPath(), HmilyParticipantUndo.class, hmilyParticipantUndo.getUndoId(), hmilyParticipantUndo);
        } catch (IOException e) {
            LogUtil.warn(LOGGER, "file {} is not exists.", () -> hmilyParticipantUndo.getUndoId());
            return HmilyRepository.FAIL_ROWS;
        }
        return HmilyRepository.ROWS;
    }

    @Override
    public List<HmilyParticipantUndo> findHmilyParticipantUndoByParticipantId(final Long participantId) {
        return listByFilter(getParticipantUndoPath(), HmilyParticipantUndo.class, (undo, params) -> {
            Long participantIdParam = (Long) params[0];
            return participantIdParam.compareTo(undo.getParticipantId()) == 0;
        }, participantId);
    }

    @Override
    public int removeHmilyParticipantUndo(final Long undoId) {
        try {
            boolean exsist = isExsist(getParticipantUndoPath(), undoId);
            if (!exsist) {
                return HmilyRepository.FAIL_ROWS;
            }
            HmilyParticipantUndo hmilyParticipantUndo = readFile(getParticipantUndoPath(), HmilyParticipantUndo.class, undoId);

            if (hmilyParticipantUndo == null) {
                return HmilyRepository.FAIL_ROWS;
            }
            boolean delete = deleteFile(getParticipantUndoPath(), undoId);
            return delete ? HmilyRepository.ROWS : HmilyRepository.FAIL_ROWS;
        } catch (IOException e) {
            LogUtil.error(LOGGER, "removeHmilyParticipantUndo occur a exception {}", e::getMessage);
        }
        return HmilyRepository.FAIL_ROWS;
    }

    @Override
    public int removeHmilyParticipantUndoByDate(final Date date) {
        return removeByFilter(getParticipantUndoPath(), HmilyParticipantUndo.class, (undo, params) -> {
            Date dateParam = (Date) params[0];
            return dateParam.after(undo.getUpdateTime()) && Objects.equals(HmilyActionEnum.DELETE.getCode(), undo.getStatus());
        }, date);
    }

    @Override
    public int updateHmilyParticipantUndoStatus(final Long undoId, final Integer status) {
        boolean exsist = isExsist(getParticipantUndoPath(), undoId);
        if (!exsist) {
            return HmilyRepository.FAIL_ROWS;
        }
        String filePath = concatPath(getParticipantUndoPath(), undoId);
        return writeParticipantUndoFile(filePath, HmilyParticipantUndo.class, undoId, status);
    }
    
    @Override
    public int writeHmilyLocks(final Collection<HmilyLock> locks) {
        // TODO
        return 0;
    }
    
    @Override
    public int releaseHmilyLocks(final Collection<HmilyLock> locks) {
        // TODO
        return 0;
    }
    
    @Override
    public Optional<HmilyLock> findHmilyLockById(final String lockId) {
        // TODO
        return Optional.empty();
    }
    
    private String getTransationPath() {
        return filePath + File.separator + HMILY_ROOT_TRANSACTION;
    }
    
    private String getParticipantPath() {
        return getTransationPath() + getParticipantPrefix() + HMILY_TRANSATION_PARTICIPANT;
    }
    
    private String getParticipantUndoPath() {
        return getTransationPath() + getParticipantPrefix() + HMILY_PARTICIPANT_UNDO;
    }
    
    private String getParticipantPrefix() {
        return File.separator + appName + File.separator;
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
                        initDir();
                        initialized = true;
                    } else if (!rootPathFile.isDirectory()) {
                        throw new HmilyRuntimeException("rootPath is not directory");
                    } else {
                        initDir();
                    }
                }
            }
        }
    }

    private void initDir() {
        File transationFileDir = new File(getTransationPath());
        if (!transationFileDir.exists()) {
            transationFileDir.getParentFile().mkdirs();
            boolean mkdirs = transationFileDir.mkdirs();
            if (!mkdirs) {
                throw new HmilyRuntimeException("cannot create transationFile path, the path to create is:" + transationFileDir.getAbsolutePath());
            }
        }
        File participantFileDir = new File(getParticipantPath());
        if (!participantFileDir.exists()) {
            participantFileDir.getParentFile().mkdirs();
            boolean mkdirs = participantFileDir.mkdirs();
            if (!mkdirs) {
                throw new HmilyRuntimeException("cannot create participantFile path, the path to create is:" + participantFileDir.getAbsolutePath());
            }
        }
        File participantUndoFileDir = new File(getParticipantUndoPath());
        if (!participantUndoFileDir.exists()) {
            participantUndoFileDir.getParentFile().mkdirs();
            boolean mkdirs = participantUndoFileDir.mkdirs();
            if (!mkdirs) {
                throw new HmilyRuntimeException("cannot create participantUndoFile path, the path to create is:" + participantUndoFileDir.getAbsolutePath());
            }
        }
    }

    private boolean isExsist(final String path, final Long transId) {
        boolean directory = Files.isDirectory(Paths.get(path));
        if (directory) {
            Path filePath = Paths.get(concatPath(path, transId));
            if (filePath.toFile().exists()) {
                return true;
            }
        }
        return false;
    }

    private <T> void createFile(final String absolutePath, final Class<T> tClass, final Long id, final T t) throws IOException {
        String filePath = concatPath(absolutePath, id);
        Files.createFile(Paths.get(filePath));
        if (!isRead(filePath)) {
            return;
        }
        LOCK.writeLock().lock();
        try {
            byte[] serialize = hmilySerializer.serialize(t);
            Files.write(Paths.get(filePath), serialize, StandardOpenOption.WRITE);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    private boolean deleteFile(final String absolutePath, final Long id) throws IOException {
        AssertUtils.notNull(id);
        return Files.deleteIfExists(Paths.get(concatPath(absolutePath, id)));
    }

    private static String concatPath(final String filePath, final Long id) {
        return filePath + File.separator + id;
    }

    @SneakyThrows
    private <T> T readFile(final String absolutePath, final Class<T> clazz, final Long id) {
        LOCK.readLock().lock();
        Path path = Paths.get(concatPath(absolutePath, id));
        try (FileChannel inChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            MappedByteBuffer mappedByteBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            byte[] dst = new byte[(int) inChannel.size()];
            mappedByteBuffer.get(dst);
            T t = hmilySerializer.deSerialize(dst, clazz);
            clean(mappedByteBuffer);
            return t;
        } catch (IOException | HmilySerializerException e) {
            LogUtil.error(LOGGER, " read file exception ,because is {}", e::getMessage);
            return null;
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @SneakyThrows
    private int writeTransactionFile(final String filePath, final Class<HmilyTransaction> clazz, final HmilyTransaction hmilyTransaction) {
        if (!isRead(filePath)) {
            return HmilyRepository.FAIL_ROWS;
        }
        LOCK.writeLock().lock();
        try {
            byte[] serialize = hmilySerializer.serialize(hmilyTransaction);
            Files.write(Paths.get(filePath), serialize, StandardOpenOption.WRITE);
            return HmilyRepository.ROWS;
        } catch (IOException | HmilySerializerException e) {
            LogUtil.error(LOGGER, " read file exception ,because is {}", e::getMessage);
            return HmilyRepository.FAIL_ROWS;
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    @SneakyThrows
    private int writeParticipantFile(final String absolutePath, final Class<HmilyParticipant> clazz, final Long participiantId, final int status, final int retryTimes) {

        String filePath = concatPath(absolutePath, participiantId);
        if (!isRead(filePath)) {
            return HmilyRepository.FAIL_ROWS;
        }
        LOCK.readLock().lock();
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            HmilyParticipant hmilyParticipant = hmilySerializer.deSerialize(bytes, clazz);
            hmilyParticipant.setStatus(status);
            hmilyParticipant.setUpdateTime(new Date());
            hmilyParticipant.setVersion(hmilyParticipant.getVersion() + 1);
            hmilyParticipant.setRetry(hmilyParticipant.getRetry() + retryTimes);
            byte[] serialize = hmilySerializer.serialize(hmilyParticipant);
            Files.write(Paths.get(filePath), serialize, StandardOpenOption.WRITE);
            return HmilyRepository.ROWS;
        } catch (IOException | HmilySerializerException e) {
            LogUtil.error(LOGGER, " read file exception ,because is {}", e::getMessage);
            return HmilyRepository.FAIL_ROWS;
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @SneakyThrows
    private int writeParticipantUndoFile(final String filePath, final Class<HmilyParticipantUndo> clazz, final Long undoId, final int status) {
        if (!isRead(filePath)) {
            return HmilyRepository.FAIL_ROWS;
        }
        LOCK.writeLock().lock();
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            HmilyParticipantUndo participantUndo = hmilySerializer.deSerialize(bytes, clazz);
            participantUndo.setStatus(status);
            participantUndo.setUpdateTime(new Date());
            byte[] serialize = hmilySerializer.serialize(participantUndo);
            Files.write(Paths.get(filePath), serialize, StandardOpenOption.WRITE);
            return HmilyRepository.ROWS;
        } catch (IOException | HmilySerializerException e) {
            LogUtil.error(LOGGER, " read file exception ,because is {}", e::getMessage);
            return HmilyRepository.FAIL_ROWS;
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    private <T> List<T> listByFilter(final String path, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            List<Path> list = Files.list(Paths.get(path)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list)) {
                return Collections.emptyList();
            }
            List<T> result = new ArrayList<>();
            list.forEach(child -> {
                if (!Files.isDirectory(child) && isRead(child.toString())
                        && child.toFile().getName().lastIndexOf(".") == -1) {
                    T t = readFile(path, deserializeClass, Long.valueOf(child.toFile().getName()));
                    if (t == null) {
                        return;
                    }
                    if (filter.filter(t, params)) {
                        result.add(t);
                    }
                }
            });
            return result;
        } catch (Exception e) {
            LogUtil.error(LOGGER, "listByFilter occur a exception {}", e::getMessage);
        }
        return Collections.emptyList();
    }

    private <T> boolean existByFilter(final Filter<HmilyParticipant> filter, final Object... params) {
        try {
            List<Path> list = Files.list(Paths.get(getParticipantPath())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list)) {
                return false;
            }
            AtomicBoolean flag = new AtomicBoolean(false);
            list.forEach(child -> {
                if (!flag.get() && !Files.isDirectory(child) && child.toFile().getName().lastIndexOf(".") == -1) {
                    HmilyParticipant hmilyParticipant = readFile(getParticipantPath(), HmilyParticipant.class, Long.valueOf(child.toFile().getName()));
                    if (hmilyParticipant == null) {
                        return;
                    }
                    if (filter.filter(hmilyParticipant, params)) {
                        flag.set(true);
                    }
                }
            });

            return flag.get();
        } catch (IOException e) {
            return false;
        }
    }

    private <T> int removeByFilter(final String filePath, final Class<T> deserializeClass, final Filter<T> filter, final Object... params) {
        try {
            List<Path> list = Files.list(Paths.get(filePath)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list)) {
                return 0;
            }
            AtomicInteger counter = new AtomicInteger();
            list.forEach(child -> {
                if (!Files.isDirectory(child) && child.toFile().getName().lastIndexOf(".") == -1) {
                    T t = readFile(filePath, deserializeClass, Long.parseLong(child.toFile().getName()));
                    if (t == null) {
                        return;
                    }
                    if (filter.filter(t, params)) {
                        try {
                            Files.deleteIfExists(child);
                            counter.incrementAndGet();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            return counter.get();
        } catch (IOException e) {
            return 0;
        }
    }

    private boolean isRead(final String filePath) {
        Path path = Paths.get(filePath);
        return Files.isReadable(path);
    }

    /**
     * The file handle is occupied help gc clean buffer.
     * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4724038
     * @param buffer buffer
     */
    public static void clean(final ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect() || buffer.capacity() == 0) {
            return;
        }
        invoke(invoke(viewed(buffer), "cleaner"), "clean");
    }

    private static Object invoke(final Object target, final String methodName, final Class<?>... args) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    Method method = method(target, methodName, args);
                    method.setAccessible(true);
                    return method.invoke(target);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    private static Method method(final Object target, final String methodName, final Class<?>[] args)
            throws NoSuchMethodException {
        try {
            return target.getClass().getMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            return target.getClass().getDeclaredMethod(methodName, args);
        }
    }

    private static ByteBuffer viewed(final ByteBuffer buffer) {
        String methodName = "viewedBuffer";
        Method[] methods = buffer.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("attachment")) {
                methodName = "attachment";
                break;
            }
        }

        ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
        if (viewedBuffer == null) {
            return buffer;
        } else {
            return viewed(viewedBuffer);
        }
    }

    /**
     * The interface Filter.
     *
     * @param <T> the type parameter
     */
    interface Filter<T> {

        /**
         * Filter boolean.
         *
         * @param t      the t
         * @param params the params
         * @return the boolean
         */
        boolean filter(T t, Object... params);
    }
}
