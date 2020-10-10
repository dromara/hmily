package org.dromara.hmily.brpc.loadbalance;

import com.baidu.brpc.client.CommunicationClient;
import com.google.common.collect.Maps;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Create by liu·yu
 * Date is 2020-10-07
 * Description is :
 */
public class HmilyLoadBalanceUtils {

    private static final Map<String, String> URL_MAP = Maps.newConcurrentMap();

    public static CommunicationClient doSelect(CommunicationClient defaultClient,
                                               List<CommunicationClient> instances) {
        final HmilyTransactionContext hmilyTransactionContext = HmilyContextHolder.get();
        if (Objects.isNull(hmilyTransactionContext)) {
            return defaultClient;
        }
        //if try
        String key = defaultClient.getCommunicationOptions().getClientName();
        if (hmilyTransactionContext.getAction() == HmilyActionEnum.TRYING.getCode()) {
            URL_MAP.put(key, defaultClient.getServiceInstance().getIp());
            return defaultClient;
        }
        final String ip = URL_MAP.get(key);
        URL_MAP.remove(key);
        if (Objects.nonNull(ip)) {
            for (CommunicationClient client : instances) {
                if (Objects.equals(client.getServiceInstance().getIp(), ip)) {
                    return client;
                }
            }
        }
        return defaultClient;
    }

}
