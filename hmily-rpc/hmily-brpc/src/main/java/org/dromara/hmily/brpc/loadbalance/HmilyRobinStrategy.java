package org.dromara.hmily.brpc.loadbalance;

import com.baidu.brpc.client.CommunicationClient;
import com.baidu.brpc.loadbalance.FairStrategy;
import com.baidu.brpc.protocol.Request;

import java.util.List;
import java.util.Set;

/**
 * Create by liu·yu
 * Date is 2020-10-08
 * Description is :
 */
public class HmilyRobinStrategy extends FairStrategy {

    @Override
    public CommunicationClient selectInstance(Request request, List<CommunicationClient> instances, Set<CommunicationClient> selectedInstances) {
        CommunicationClient client = super.selectInstance(request, instances, selectedInstances);
        return HmilyLoadBalanceUtils.doSelect(client, instances);
    }
}
