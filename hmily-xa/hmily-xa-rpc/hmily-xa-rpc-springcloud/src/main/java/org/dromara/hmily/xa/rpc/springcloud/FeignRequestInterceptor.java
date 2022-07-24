package org.dromara.hmily.xa.rpc.springcloud;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.context.XaParticipant;
import org.dromara.hmily.core.mediator.RpcMediator;
import org.dromara.hmily.xa.core.XidImpl;
import org.dromara.hmily.xa.rpc.RpcXaProxy;
import org.springframework.core.Ordered;

import javax.transaction.xa.Xid;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 保证如果有context，就一定会设置
 * 保存的是远程rpc产生的，而有rpc则一定会commit或rollback
 */
class FeignRequestInterceptor implements RequestInterceptor, Ordered {
    private final ThreadLocal<Map<Xid, String>> map = ThreadLocal.withInitial (HashMap::new);

    public static void main(String[] args) {
        RequestTemplate template=new RequestTemplate ();
        System.out.println (template.url ());
        template.insert (0,"http://xxx.com:8899/");
        System.out.println (template.url ());
        template.insert (0,"http://xxx.com:8899/");
        System.out.println (template.url ());
    }

    @Override
    public void apply(RequestTemplate template) {
        HmilyTransactionContext context = HmilyContextHolder.get ();
        if (context == null)
            return;

        RpcMediator.getInstance ().transmit (template::header, context);

        //处理负载均衡
//        XaParticipant participant = context.getXaParticipant ();
//        Xid xid = new XidImpl (participant.getBranchId ());
//        Map<Xid, String> map = this.map.get ();
//
//        if (map.containsKey (xid)) {
//            template.target
//        }
//
//        String cmd = participant.getCmd ();
//        if (Objects.equals (cmd, RpcXaProxy.XaCmd.COMMIT.name ()) ||
//                Objects.equals (cmd, RpcXaProxy.XaCmd.ROLLBACK.name ())) {
//            map.remove (xid);
//        } else if (Objects.equals (cmd, RpcXaProxy.XaCmd.START.name ())) {
//            map.put (xid, template.url ());
//        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
