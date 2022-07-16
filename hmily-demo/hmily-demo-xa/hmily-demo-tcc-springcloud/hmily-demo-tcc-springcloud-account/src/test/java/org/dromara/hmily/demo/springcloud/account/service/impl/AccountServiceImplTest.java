package org.dromara.hmily.demo.springcloud.account.service.impl;


import org.dromara.hmily.common.utils.IdWorkerUtils;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.context.XaParticipant;
import org.dromara.hmily.demo.common.account.dto.AccountDTO;
import org.dromara.hmily.demo.springcloud.account.controller.AccountController;
import org.dromara.hmily.xa.core.XidImpl;
import org.dromara.hmily.xa.rpc.RpcXaProxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.xa.Xid;
import java.math.BigDecimal;

@SpringBootTest
public class AccountServiceImplTest {
    @Autowired
    private AccountController service;

    @Test
    public void test2() {
        Xid xid = new XidImpl ();
        System.out.println (new String (xid.getBranchQualifier ()));
        System.out.println (new String (xid.getGlobalTransactionId ()));

//        Xid xid2 = new XidImpl (new String (xid.getBranchQualifier ()));
//        System.out.println (xid2);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println (IdWorkerUtils.getInstance ().createUUID ());
        }
    }

    @Test
    public void testTestPayment() {
        HmilyTransactionContext context = new HmilyTransactionContext ();
        XaParticipant participant = new XaParticipant ();
        Xid xid = new XidImpl ();
        participant.setCmd (RpcXaProxy.XaCmd.START.name ());
        participant.setBranchId (new String (xid.getBranchQualifier ()));
        participant.setGlobalId (new String (xid.getGlobalTransactionId ()));
        System.out.println (participant.getBranchId ());

        context.setXaParticipant (participant);
        HmilyContextHolder.set (context);

        AccountDTO dto = new AccountDTO ();
        dto.setUserId ("1");
        dto.setAmount (BigDecimal.valueOf (1.1));
        service.testPayment (dto);
    }

    @Test
    public void testNo() {
        AccountDTO dto = new AccountDTO ();
        dto.setUserId ("1");
        dto.setAmount (BigDecimal.valueOf (1.1));
        service.testPayment (dto);
    }
}
