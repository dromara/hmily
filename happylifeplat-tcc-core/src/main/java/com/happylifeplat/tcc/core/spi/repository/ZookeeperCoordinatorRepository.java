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
import com.happylifeplat.tcc.common.config.TccConfig;
import com.happylifeplat.tcc.common.config.TccZookeeperConfig;
import com.happylifeplat.tcc.core.bean.entity.TccTransaction;
import com.happylifeplat.tcc.common.enums.RepositorySupportEnum;
import com.happylifeplat.tcc.common.exception.TccException;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;
import com.happylifeplat.tcc.common.utils.LogUtil;
import com.happylifeplat.tcc.core.spi.CoordinatorRepository;
import com.happylifeplat.tcc.core.spi.ObjectSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;


public class ZookeeperCoordinatorRepository implements CoordinatorRepository {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperCoordinatorRepository.class);

    private ObjectSerializer objectSerializer;

    private String rootPath = "/tcc";

    private String modelName;

    private static volatile ZooKeeper zooKeeper;

    private static final CountDownLatch countDownLatch = new CountDownLatch(1);


    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * 创建本地事务对象
     *
     * @param tccTransaction 事务对象
     * @return rows
     */

    @Override
    public int create(TccTransaction tccTransaction) {
        try {
            zooKeeper.create(getRootPath(tccTransaction.getTransId()),
                    objectSerializer.serialize(tccTransaction),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            return 1;
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    /**
     * 删除对象
     *
     * @param id 事务对象id
     * @return rows
     */
    @Override
    public int remove(String id) {
        try {
            final TccTransaction byId = findById(id);
            zooKeeper.delete(getRootPath(id), -1);
            return 1;
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    /**
     * 更新数据
     *
     * @param tccTransaction 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     */
    @Override
    public int update(TccTransaction tccTransaction) throws TccRuntimeException {
        try {
            tccTransaction.setLastTime(new Date());
            tccTransaction.setVersion(tccTransaction.getVersion() + 1);
            zooKeeper.setData(getRootPath(tccTransaction.getTransId()),
                    objectSerializer.serialize(tccTransaction), -1);
            return 1;
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    /**
     * 根据id获取对象
     *
     * @param id 主键id
     * @return TransactionRecover
     */
    @Override
    public TccTransaction findById(String id) {
        try {
            Stat stat = new Stat();
            byte[] content = zooKeeper.getData(getRootPath(id), false, stat);
            return objectSerializer.deSerialize(content, TccTransaction.class);
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
    }

    /**
     * 获取需要提交的事务
     *
     * @return List<TransactionRecover>
     */
    @Override
    public List<TccTransaction> listAll() {
        List<TccTransaction> transactionRecovers = Lists.newArrayList();

        List<String> zNodePaths;
        try {
            zNodePaths = zooKeeper.getChildren(rootPath, false);
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }
        if (CollectionUtils.isNotEmpty(zNodePaths)) {
            transactionRecovers = zNodePaths.stream()
                    .filter(StringUtils::isNoneBlank)
                    .map(zNodePath -> {
                        try {
                            byte[] content = zooKeeper.getData(getRootPath(zNodePath), false, new Stat());
                            return objectSerializer.deSerialize(content, TccTransaction.class);
                        } catch (KeeperException | InterruptedException | TccException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).collect(Collectors.toList());
        }

        return transactionRecovers;
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
        return tccTransactions.stream().filter(tccTransaction -> tccTransaction.getLastTime().compareTo(date) > 0).collect(Collectors.toList());
    }

    /**
     * 初始化操作
     *
     * @param modelName 模块名称
     * @param tccConfig 配置信息
     */
    @Override
    public void init(String modelName, TccConfig tccConfig) {
        this.modelName = modelName;
        final String zkRootPath = tccConfig.getTccZookeeperConfig().getRootPath();
        if (StringUtils.isNoneBlank(zkRootPath)) {
            String path = String.join("_", zkRootPath, modelName);
            setRootPath(path);
        } else {
            setRootPath(String.join("_", this.rootPath, modelName));
        }

        try {
            connect(tccConfig.getTccZookeeperConfig());
        } catch (Exception e) {
            LogUtil.error(LOGGER, "zookeeper连接异常请检查配置信息是否正确:{}", e::getMessage);
            throw new TccRuntimeException(e.getMessage());
        }


    }

    private void connect(TccZookeeperConfig config) {
        try {
            zooKeeper = new ZooKeeper(config.getHost(), config.getSessionTimeOut(), watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    // 放开闸门, wait在connect方法上的线程将被唤醒
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            Stat stat = zooKeeper.exists(rootPath, false);
            if (stat == null) {
                zooKeeper.create(rootPath, rootPath.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            throw new TccRuntimeException(e);
        }


    }

    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return RepositorySupportEnum.ZOOKEEPER.getSupport();
    }

    /**
     * 设置序列化信息
     *
     * @param objectSerializer 序列化实现
     */
    @Override
    public void setSerializer(ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

    private String getParentPath() {
        return String.join("/", rootPath, modelName);
    }

    private String getRootPath(String id) {
        return String.join("/", rootPath, id);
    }


}
