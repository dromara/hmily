package org.dromara.hmily.demo.tac.dubbo.order.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The enum transaction enum.
 *
 * @author zhangzhi
 */
@RequiredArgsConstructor
@Getter
public enum ReadCommittedTransactionEnum {

    TRANSACTION_READ_WRITE(1, "读已提交隔离级别的事务, 包括更新、查询操作"),

    TRANSACTION_READ_ONLY(2, "读已提交隔离级别的事务, 只有查询操作");

    private final int code;

    private final String desc;
}
