package org.dromara.hmily.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The enum Transaction Isolation Level enum.
 *
 * @author zhangzhi
 */
@AllArgsConstructor
@Getter
public enum IsolationLevelEnum {

    /**
     * read_uncommitted enum.
     */
    READ_UNCOMMITTED(0),

    /**
     * read_committed enum.
     */
    READ_COMMITTED(1);

    private final int value;
}
