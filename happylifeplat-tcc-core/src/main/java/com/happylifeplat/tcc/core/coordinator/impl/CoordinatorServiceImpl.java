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

package com.happylifeplat.tcc.core.coordinator.impl;


import com.google.common.collect.Lists;
import com.happylifeplat.tcc.annotation.TccPatternEnum;
import com.happylifeplat.tcc.common.config.TccConfig;
import com.happylifeplat.tcc.common.enums.CoordinatorActionEnum;
import com.happylifeplat.tcc.common.enums.TccActionEnum;
import com.happylifeplat.tcc.common.enums.TccRoleEnum;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;
import com.happylifeplat.tcc.common.utils.LogUtil;
import com.happylifeplat.tcc.common.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.common.bean.entity.Participant;
import com.happylifeplat.tcc.common.bean.entity.TccInvocation;
import com.happylifeplat.tcc.common.bean.entity.TccTransaction;
import com.happylifeplat.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.happylifeplat.tcc.core.concurrent.threadpool.TccTransactionThreadFactory;
import com.happylifeplat.tcc.core.concurrent.threadpool.TccTransactionThreadPool;
import com.happylifeplat.tcc.core.coordinator.CoordinatorService;
import com.happylifeplat.tcc.core.coordinator.command.CoordinatorAction;
import com.happylifeplat.tcc.core.helper.SpringBeanUtils;
import com.happylifeplat.tcc.core.service.ApplicationService;
import com.happylifeplat.tcc.core.spi.CoordinatorRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xiaoyu
 */
@Service("coordinatorService")
public class CoordinatorServiceImpl implements CoordinatorService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinatorServiceImpl.class);


    private static BlockingQueue<CoordinatorAction> QUEUE;

    private TccConfig tccConfig;


    private CoordinatorRepository coordinatorRepository;

    private final ApplicationService applicationService;

    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    public CoordinatorServiceImpl(ApplicationService applicationService) {
        this.applicationService = applicationService;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1,
                TccTransactionThreadFactory.create("tccRollBackService", true));

    }


    /**
     * 初始化协调资源
     *
     * @param tccConfig 配置信息
     * @throws Exception 异常
     */
    @Override
    public void start(TccConfig tccConfig) throws Exception {
        this.tccConfig = tccConfig;
        final String appName = applicationService.acquireName();
        coordinatorRepository = SpringBeanUtils.getInstance().getBean(CoordinatorRepository.class);
        //初始化spi 协调资源存储
        coordinatorRepository.init(appName, tccConfig);
        //初始化 协调资源线程池
        initCoordinatorPool();
        //定时执行补偿
        scheduledRollBack();

    }


    /**
     * 保存补偿事务信息
     *
     * @param tccTransaction 实体对象
     * @return 主键id
     */
    @Override
    public String save(TccTransaction tccTransaction) {
        final int rows = coordinatorRepository.create(tccTransaction);
        if (rows > 0) {
            return tccTransaction.getTransId();
        }
        return null;
    }

    @Override
    public TccTransaction findByTransId(String transId) {
        return coordinatorRepository.findById(transId);
    }

    /**
     * 删除补偿事务信息
     *
     * @param id 主键id
     * @return true成功 false 失败
     */
    @Override
    public boolean remove(String id) {
        return coordinatorRepository.remove(id) > 0;
    }

    /**
     * 更新
     *
     * @param tccTransaction 实体对象
     */
    @Override
    public void update(TccTransaction tccTransaction) {
        coordinatorRepository.update(tccTransaction);
    }

    /**
     * 更新 List<Participant>  只更新这一个字段数据
     *
     * @param tccTransaction 实体对象
     */
    @Override
    public int updateParticipant(TccTransaction tccTransaction) {
        return coordinatorRepository.updateParticipant(tccTransaction);
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
        return coordinatorRepository.updateStatus(id, status);
    }

    /**
     * 提交补偿操作
     *
     * @param coordinatorAction 执行动作
     */
    @Override
    public Boolean submit(CoordinatorAction coordinatorAction) {
        try {
            QUEUE.put(coordinatorAction);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    private void initCoordinatorPool() {
        synchronized (LOGGER) {
            QUEUE = new LinkedBlockingQueue<>(tccConfig.getCoordinatorQueueMax());
            final int coordinatorThreadMax = tccConfig.getCoordinatorThreadMax();
            final TccTransactionThreadPool threadPool = SpringBeanUtils.getInstance().getBean(TccTransactionThreadPool.class);
            final ExecutorService executorService = threadPool.newCustomFixedThreadPool(coordinatorThreadMax);
            LogUtil.info(LOGGER, "启动协调资源操作线程数量为:{}", () -> coordinatorThreadMax);
            for (int i = 0; i < coordinatorThreadMax; i++) {
                executorService.execute(new Worker());
            }

        }
    }


    /**
     * 线程执行器
     */
    class Worker implements Runnable {

        @Override
        public void run() {
            execute();
        }

        private void execute() {
            while (true) {
                try {
                    final CoordinatorAction coordinatorAction = QUEUE.take();
                    if (coordinatorAction != null) {
                        final int code = coordinatorAction.getAction().getCode();
                        if (CoordinatorActionEnum.SAVE.getCode() == code) {
                            save(coordinatorAction.getTccTransaction());
                        } else if (CoordinatorActionEnum.DELETE.getCode() == code) {
                            remove(coordinatorAction.getTccTransaction().getTransId());
                        } else if (CoordinatorActionEnum.UPDATE.getCode() == code) {
                            update(coordinatorAction.getTccTransaction());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.error(LOGGER, "执行协调命令失败：{}", e::getMessage);
                }
            }

        }
    }


    private void scheduledRollBack() {
        scheduledExecutorService
                .scheduleWithFixedDelay(() -> {
                    LogUtil.debug(LOGGER, "rollback execute delayTime:{}", () -> tccConfig.getScheduledDelay());
                    try {
                        final List<TccTransaction> tccTransactions =
                                coordinatorRepository.listAllByDelay(acquireData());
                        if (CollectionUtils.isNotEmpty(tccTransactions)) {

                            for (TccTransaction tccTransaction : tccTransactions) {

                                //如果try未执行完成，那么就不进行补偿 （防止在try阶段的各种异常情况）
                                if (tccTransaction.getRole() == TccRoleEnum.PROVIDER.getCode() &&
                                        tccTransaction.getStatus() == TccActionEnum.PRE_TRY.getCode()) {
                                    continue;
                                }

                                if (tccTransaction.getRetriedCount() > tccConfig.getRetryMax()) {
                                    LogUtil.error(LOGGER, "此事务超过了最大重试次数，不再进行重试：{}",
                                            () -> tccTransaction);
                                    continue;
                                }
                                if (Objects.equals(tccTransaction.getPattern(), TccPatternEnum.CC.getCode())
                                        && tccTransaction.getStatus() == TccActionEnum.TRYING.getCode()) {
                                    continue;
                                }

                                //如果事务角色是提供者的话，并且在重试的次数范围类是不能执行的，只能由发起者执行
                                if (tccTransaction.getRole() == TccRoleEnum.PROVIDER.getCode()
                                        && (tccTransaction.getCreateTime().getTime() +
                                        tccConfig.getRetryMax() * tccConfig.getRecoverDelayTime() * 1000
                                        > System.currentTimeMillis())) {
                                    continue;
                                }

                                try {
                                    // 先更新数据，然后执行
                                    tccTransaction.setRetriedCount(tccTransaction.getRetriedCount() + 1);
                                    final int rows = coordinatorRepository.update(tccTransaction);
                                    //判断当rows>0 才执行，为了防止业务方为集群模式时候的并发
                                    if (rows > 0) {
                                        //如果是以下3种状态
                                        if ((tccTransaction.getStatus() == TccActionEnum.TRYING.getCode()
                                                || tccTransaction.getStatus() == TccActionEnum.PRE_TRY.getCode()
                                                || tccTransaction.getStatus() == TccActionEnum.CANCELING.getCode())) {
                                            cancel(tccTransaction);
                                        } else if (tccTransaction.getStatus() == TccActionEnum.CONFIRMING.getCode()) {
                                            //执行confirm操作
                                            confirm(tccTransaction);
                                        }
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    LogUtil.error(LOGGER, "执行事务补偿异常:{}", e::getMessage);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 30, tccConfig.getScheduledDelay(), TimeUnit.SECONDS);

    }

    private void cancel(TccTransaction tccTransaction) {
        final List<Participant> participants = tccTransaction.getParticipants();
        List<Participant> failList = Lists.newArrayListWithCapacity(participants.size());
        boolean success = true;
        if (CollectionUtils.isNotEmpty(participants)) {
            for (Participant participant : participants) {
                try {
                    TccTransactionContext context = new TccTransactionContext();
                    context.setAction(TccActionEnum.CANCELING.getCode());
                    context.setTransId(tccTransaction.getTransId());
                    TransactionContextLocal.getInstance().set(context);
                    executeCoordinator(participant.getCancelTccInvocation());
                } catch (Exception e) {
                    LogUtil.error(LOGGER, "执行cancel方法异常:{}", () -> e);
                    success = false;
                    failList.add(participant);
                }
            }
            executeHandler(success, tccTransaction, failList);
        }

    }

    private void confirm(TccTransaction tccTransaction) {

        final List<Participant> participants = tccTransaction.getParticipants();

        List<Participant> failList = Lists.newArrayListWithCapacity(participants.size());
        boolean success = true;
        if (CollectionUtils.isNotEmpty(participants)) {
            for (Participant participant : participants) {
                try {
                    TccTransactionContext context = new TccTransactionContext();
                    context.setAction(TccActionEnum.CONFIRMING.getCode());
                    context.setTransId(tccTransaction.getTransId());
                    TransactionContextLocal.getInstance().set(context);
                    executeCoordinator(participant.getConfirmTccInvocation());
                } catch (Exception e) {
                    LogUtil.error(LOGGER, "执行confirm方法异常:{}", () -> e);
                    success = false;
                    failList.add(participant);
                }
            }
            executeHandler(success, tccTransaction, failList);
        }

    }


    private void executeHandler(boolean success, final TccTransaction currentTransaction,
                                List<Participant> failList) {
        if (success) {
            coordinatorRepository.remove(currentTransaction.getTransId());
        } else {
            currentTransaction.setParticipants(failList);
            coordinatorRepository.updateParticipant(currentTransaction);
        }
    }


    @SuppressWarnings("unchecked")
    private void executeCoordinator(TccInvocation tccInvocation) throws Exception {
        if (Objects.nonNull(tccInvocation)) {
            final Class clazz = tccInvocation.getTargetClass();
            final String method = tccInvocation.getMethodName();
            final Object[] args = tccInvocation.getArgs();
            final Class[] parameterTypes = tccInvocation.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);
            LogUtil.debug(LOGGER, "执行本地协调事务:{}", () -> tccInvocation.getTargetClass()
                    + ":" + tccInvocation.getMethodName());
        }
    }


    private Date acquireData() {
        return new Date(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - (tccConfig.getRecoverDelayTime() * 1000));

    }


}
