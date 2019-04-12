package org.dromara.hmily.core.disruptor;

import lombok.Data;

/**
 * DataEvent.
 * disruptor data carrier .
 * @author chenbin sixh
 */
@Data
public class DataEvent<T> {

    private T t;
}
