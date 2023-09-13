package org.dromara.hmily.tac.core.lock;

import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.tac.core.exception.LockWaitTimeoutException;

/**
 * Lock retry controller.
 *
 * @author zhangzhi
 */
@Slf4j
public class HmilyLockRetryHandler {

    private int lockRetryInterval;

    private int lockRetryTimes;

    /**
     * Instantiates a new Lock retry controller.
     */
    public HmilyLockRetryHandler(final int lockRetryInterval, final int lockRetryTimes) {
        HmilyConfig hmilyConfig = ConfigEnv.getInstance().getConfig(HmilyConfig.class);
        this.lockRetryInterval = lockRetryInterval <= 0 ? hmilyConfig.getLockRetryInterval() : lockRetryInterval;
        this.lockRetryTimes = lockRetryTimes < 0 ? hmilyConfig.getLockRetryTimes() : lockRetryTimes;
    }

    /**
     * Sleep.
     * @param e the e
     * @throws LockWaitTimeoutException the lock wait timeout exception
     */
    public void sleep(final Exception e) {
        if (--lockRetryTimes < 0) {
            log.error("Global lock wait timeout");
            throw new LockWaitTimeoutException("Global lock wait timeout", e);
        }
        try {
            Thread.sleep(lockRetryInterval);
        } catch (InterruptedException ignore) {
        }
    }
}
