package org.dromara.hmily.demo.grpc.account;

import org.dromara.hmily.demo.common.account.dto.AccountDTO;

/**
 * @author tydhot
 */
public interface AccountServiceBean {
    
    boolean payment(AccountDTO accountDTO);
    
}
