/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dromara.hmily.xa.core.recovery;

import javax.transaction.xa.XAResource;

/**
 * XaResourceRecovery .
 * Transaction recovery processing.
 *
 * @author sixh chenbin
 */
public interface XaResourceRecovery {

    /**
     * 提交一个恢复日志log.
     *
     * @param recoveryLog the recovery log
     */
    void commitLog(RecoveryLog recoveryLog);

    /**
     * Rollback log.
     *
     * @param recoveryLog the recovery log
     */
    void rollbackLog(RecoveryLog recoveryLog);

    /**
     * 开始一个事务的恢复.
     *
     * @param resource the resource
     */
    void recover(XAResource resource);
}
