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

package org.dromara.hmily.core.spi.repository;

import com.google.common.collect.Lists;
import org.dromara.hmily.annotation.HmilySPI;
import org.dromara.hmily.common.bean.adapter.CoordinatorRepositoryAdapter;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.config.HmilyConfig;
import org.dromara.hmily.common.enums.RepositorySupportEnum;
import org.dromara.hmily.common.exception.HmilyException;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.serializer.ObjectSerializer;
import org.dromara.hmily.common.utils.FileUtils;
import org.dromara.hmily.common.utils.RepositoryConvertUtils;
import org.dromara.hmily.common.utils.RepositoryPathUtils;
import org.dromara.hmily.core.spi.HmilyCoordinatorRepository;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * file impl.
 * @author xiaoyu
 */
@SuppressWarnings("all")
@HmilySPI("file")
public class FileCoordinatorRepository implements HmilyCoordinatorRepository {

    private static volatile boolean initialized;

    private String filePath;

    private ObjectSerializer serializer;

    @Override
    public void setSerializer(final ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public int create(final HmilyTransaction hmilyTransaction) {

        writeFile(hmilyTransaction);
        return 1;
    }

    @Override
    public int remove(final String id) {
        String fullFileName = RepositoryPathUtils.getFullFileName(filePath, id);
        File file = new File(fullFileName);
        if (file.exists()) {
            file.delete();
        }
        return ROWS;
    }

    @Override
    public int update(final HmilyTransaction hmilyTransaction) throws HmilyRuntimeException {
        hmilyTransaction.setLastTime(new Date());
        hmilyTransaction.setVersion(hmilyTransaction.getVersion() + 1);
        hmilyTransaction.setRetriedCount(hmilyTransaction.getRetriedCount() + 1);
        try {
            writeFile(hmilyTransaction);
        } catch (Exception e) {
            throw new HmilyRuntimeException("update data exception!");
        }
        return 1;
    }

    @Override
    public int updateParticipant(final HmilyTransaction hmilyTransaction) {
        try {
            final String fullFileName = RepositoryPathUtils.getFullFileName(filePath, hmilyTransaction.getTransId());
            final File file = new File(fullFileName);
            final CoordinatorRepositoryAdapter adapter = readAdapter(file);
            if (Objects.nonNull(adapter)) {
                adapter.setContents(serializer.serialize(hmilyTransaction.getHmilyParticipants()));
            }
            FileUtils.writeFile(fullFileName, serializer.serialize(adapter));
        } catch (Exception e) {
            throw new HmilyRuntimeException("update data exception!");
        }
        return ROWS;
    }

    @Override
    public int updateStatus(final String id, final Integer status) {
        try {
            final String fullFileName = RepositoryPathUtils.getFullFileName(filePath, id);
            final File file = new File(fullFileName);
            final CoordinatorRepositoryAdapter adapter = readAdapter(file);
            if (Objects.nonNull(adapter)) {
                adapter.setStatus(status);
            }
            FileUtils.writeFile(fullFileName, serializer.serialize(adapter));
        } catch (Exception e) {
            throw new HmilyRuntimeException("update data exception!");
        }
        return ROWS;
    }

    @Override
    public HmilyTransaction findById(final String id) {
        String fullFileName = RepositoryPathUtils.getFullFileName(filePath, id);
        File file = new File(fullFileName);
        try {
            return readTransaction(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<HmilyTransaction> listAll() {
        List<HmilyTransaction> transactionRecoverList = Lists.newArrayList();
        File path = new File(filePath);
        File[] files = path.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                try {
                    HmilyTransaction transaction = readTransaction(file);
                    transactionRecoverList.add(transaction);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return transactionRecoverList;
    }

    @Override
    public List<HmilyTransaction> listAllByDelay(final Date date) {
        final List<HmilyTransaction> hmilyTransactions = listAll();
        return hmilyTransactions.stream()
                .filter(tccTransaction -> tccTransaction.getLastTime().compareTo(date) < 0)
                .collect(Collectors.toList());
    }

    @Override
    public void init(final String modelName, final HmilyConfig hmilyConfig) {
        filePath = RepositoryPathUtils.buildFilePath(modelName);
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.mkdirs();
        }
    }

    @Override
    public String getScheme() {
        return RepositorySupportEnum.FILE.getSupport();
    }

    private void writeFile(final HmilyTransaction hmilyTransaction) {
        makeDir();
        String fileName = RepositoryPathUtils.getFullFileName(filePath, hmilyTransaction.getTransId());
        try {
            FileUtils.writeFile(fileName, RepositoryConvertUtils.convert(hmilyTransaction, serializer));
        } catch (HmilyException e) {
            e.printStackTrace();
        }
    }

    private HmilyTransaction readTransaction(final File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            fis.read(content);
            return RepositoryConvertUtils.transformBean(content, serializer);
        }
    }

    private CoordinatorRepositoryAdapter readAdapter(final File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            fis.read(content);
            return serializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
        }
    }

    private void makeDir() {
        if (!initialized) {
            synchronized (FileCoordinatorRepository.class) {
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
