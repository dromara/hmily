/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.happylifeplat.tcc.core.spi.repository;

import com.google.common.collect.Lists;
import com.happylifeplat.tcc.common.bean.adapter.CoordinatorRepositoryAdapter;
import com.happylifeplat.tcc.common.bean.entity.TccTransaction;
import com.happylifeplat.tcc.common.config.TccConfig;
import com.happylifeplat.tcc.common.config.TccFileConfig;
import com.happylifeplat.tcc.common.enums.RepositorySupportEnum;
import com.happylifeplat.tcc.common.exception.TccException;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;
import com.happylifeplat.tcc.common.serializer.ObjectSerializer;
import com.happylifeplat.tcc.common.utils.FileUtils;
import com.happylifeplat.tcc.common.utils.RepositoryConvertUtils;
import com.happylifeplat.tcc.common.utils.RepositoryPathUtils;
import com.happylifeplat.tcc.core.spi.CoordinatorRepository;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @author xiaoyu
 */
@SuppressWarnings("unchecked")
public class FileCoordinatorRepository implements CoordinatorRepository {


    private String filePath;

    private volatile static boolean initialized;


    private ObjectSerializer serializer;

    @Override
    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 创建本地事务对象
     *
     * @param tccTransaction 事务对象
     * @return rows
     */
    @Override
    public int create(TccTransaction tccTransaction) {
        writeFile(tccTransaction);
        return 1;
    }

    /**
     * 删除对象
     *
     * @param id 事务对象id
     * @return rows
     */
    @Override
    public int remove(String id) {
        String fullFileName = RepositoryPathUtils.getFullFileName(filePath, id);
        File file = new File(fullFileName);
        if (file.exists()) {
            file.delete();
        }
        return 1;
    }

    /**
     * 更新数据
     *
     * @param tccTransaction 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     */
    @Override
    public int update(TccTransaction tccTransaction) throws TccRuntimeException {
        tccTransaction.setLastTime(new Date());
        tccTransaction.setVersion(tccTransaction.getVersion() + 1);
        tccTransaction.setRetriedCount(tccTransaction.getRetriedCount() + 1);
        try {
            writeFile(tccTransaction);
        } catch (Exception e) {
            throw new TccRuntimeException("更新数据异常！");
        }
        return 1;
    }

    /**
     * 更新 List<Participant>  只更新这一个字段数据
     *
     * @param tccTransaction 实体对象
     */
    @Override
    public int updateParticipant(TccTransaction tccTransaction) {
        try {

            final String fullFileName = RepositoryPathUtils.getFullFileName(filePath,tccTransaction.getTransId());
            final File file = new File(fullFileName);
            final CoordinatorRepositoryAdapter adapter = readAdapter(file);
            if(Objects.nonNull(adapter)){
                adapter.setContents(serializer.serialize(tccTransaction.getParticipants()));
            }
            FileUtils.writeFile(fullFileName,serializer.serialize(adapter));

        } catch (Exception e) {
            throw new TccRuntimeException("更新数据异常！");
        }
        return 1;
    }

    /**
     * 更新补偿数据状态
     *
     * @param id     事务id
     * @param status 状态
     * @return rows 1 成功 0 失败
     */
    @Override
    public int updateStatus(String id, Integer status) {
        try {

            final String fullFileName = RepositoryPathUtils.getFullFileName(filePath,id);
            final File file = new File(fullFileName);
            final CoordinatorRepositoryAdapter adapter = readAdapter(file);
            if(Objects.nonNull(adapter)){
                adapter.setStatus(status);
            }
            FileUtils.writeFile(fullFileName,serializer.serialize(adapter));

        } catch (Exception e) {
            throw new TccRuntimeException("更新数据异常！");
        }
        return 1;
    }


    /**
     * 根据id获取对象
     *
     * @param id 主键id
     * @return TccTransaction
     */
    @Override
    public TccTransaction findById(String id) {
        String fullFileName = RepositoryPathUtils.getFullFileName(filePath, id);
        File file = new File(fullFileName);
        try {
            return readTransaction(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 获取需要提交的事务
     *
     * @return List<TransactionRecover>
     */
    @Override
    public List<TccTransaction> listAll() {
        List<TccTransaction> transactionRecoverList = Lists.newArrayList();
        File path = new File(filePath);
        File[] files = path.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                try {
                    TccTransaction  transaction = readTransaction(file);
                    transactionRecoverList.add(transaction);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return transactionRecoverList;
    }

    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<TccTransaction>
     */
    @Override
    public List<TccTransaction> listAllByDelay(Date date) {
        final List<TccTransaction> tccTransactions = listAll();
        return tccTransactions.stream()
                .filter(tccTransaction -> tccTransaction.getLastTime().compareTo(date) < 0)
                .collect(Collectors.toList());
    }


    @Override
    public void init(String modelName, TccConfig tccConfig) {
        filePath = RepositoryPathUtils.buildFilePath(modelName);

        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.mkdirs();
        }
    }

    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return RepositorySupportEnum.FILE.getSupport();
    }

    private void writeFile(TccTransaction tccTransaction) {
        makeDirIfNecessory();

        String fileName = RepositoryPathUtils.getFullFileName(filePath, tccTransaction.getTransId());

        try {
            FileUtils.writeFile(fileName, RepositoryConvertUtils.convert(tccTransaction, serializer));
        } catch (TccException e) {
            e.printStackTrace();
        }

    }


    private TccTransaction readTransaction(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            fis.read(content);
            return RepositoryConvertUtils.transformBean(content, serializer);
        }

    }

    private CoordinatorRepositoryAdapter readAdapter(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            fis.read(content);
            return serializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
        }

    }

    private void makeDirIfNecessory() {
        if (!initialized) {
            synchronized (FileCoordinatorRepository.class) {
                if (!initialized) {
                    File rootPathFile = new File(filePath);
                    if (!rootPathFile.exists()) {

                        boolean result = rootPathFile.mkdir();

                        if (!result) {
                            throw new TccRuntimeException("cannot create root path, the path to create is:" + filePath);
                        }

                        initialized = true;
                    } else if (!rootPathFile.isDirectory()) {
                        throw new TccRuntimeException("rootPath is not directory");
                    }
                }
            }
        }
    }
}
