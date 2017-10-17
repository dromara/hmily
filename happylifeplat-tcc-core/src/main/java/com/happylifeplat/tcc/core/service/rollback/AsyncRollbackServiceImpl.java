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

package com.happylifeplat.tcc.core.service.rollback;

import com.happylifeplat.tcc.common.enums.TccActionEnum;
import com.happylifeplat.tcc.common.utils.LogUtil;
import com.happylifeplat.tcc.core.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.core.bean.entity.Participant;
import com.happylifeplat.tcc.core.bean.entity.TccInvocation;
import com.happylifeplat.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.happylifeplat.tcc.core.helper.SpringBeanUtils;
import com.happylifeplat.tcc.core.service.RollbackService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author xiaoyu
 */
@Component
@SuppressWarnings("unchecked")
public class AsyncRollbackServiceImpl implements RollbackService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncRollbackServiceImpl.class);

    /**
     * 执行协调回滚方法
     *
     * @param participantList 需要协调的资源集合
     */
    @Override
    public void execute(List<Participant> participantList) {
        try {
            if (CollectionUtils.isNotEmpty(participantList)) {
                final CompletableFuture[] cfs = participantList
                        .stream()
                        .map(participant ->
                                CompletableFuture.runAsync(() -> {
                                    TccTransactionContext context = new TccTransactionContext();
                                    context.setAction(TccActionEnum.CANCELING.getCode());
                                    context.setTransId(participant.getTransId());
                                    TransactionContextLocal.getInstance().set(context);
                                    try {
                                        executeParticipantMethod(participant.getCancelTccInvocation());

                                    } catch (Exception e) {
                                        LogUtil.error(LOGGER, "执行cancel方法异常：{}", e::getMessage);
                                        e.printStackTrace();
                                    }
                                }).whenComplete((v, e) -> TransactionContextLocal.getInstance().remove()))
                        .toArray(CompletableFuture[]::new);
                CompletableFuture.allOf(cfs).join();
            }
            LogUtil.debug(LOGGER, () -> "执行cancel方法成功！");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(LOGGER, "执行cancel方法异常：{}", e::getMessage);
        }

    }

    private void executeParticipantMethod(TccInvocation tccInvocation) throws Exception {
        if (Objects.nonNull(tccInvocation)) {
            final Class clazz = tccInvocation.getTargetClass();
            final String method = tccInvocation.getMethodName();
            final Object[] args = tccInvocation.getArgs();
            final Class[] parameterTypes = tccInvocation.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            LogUtil.debug(LOGGER, "开始执行：{}", () -> clazz.getName() + " ;" + method);
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);

        }
    }
}
