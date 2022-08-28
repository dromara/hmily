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

package org.dromara.hmily.xa.rpc;

import org.dromara.hmily.core.context.XaParticipant;

import javax.transaction.xa.Xid;
import java.util.Map;

/**
 * RpcXaProxy .
 * 定义一个代理处理rpc相关的信息.
 *
 * @author sixh chenbin
 */
public interface RpcXaProxy {

    /**
     * 执行成功.
     */
    int YES = 10000;

    /**
     * 执行有异常了.
     */
    int EXC = 10001;

    /**
     * 执行不成功.
     */
    int NO = 10002;

    /**
     * 会调用远程执行的一些命令.
     *
     * @param cmd    the cmd
     * @param params the params
     * @return the int
     * @see XaCmd
     */
    Integer cmd(XaCmd cmd, Map<String, Object> params);

    /**
     * 获取一个超时的时间,这里返回的也就是一个Rpc timeout.
     *
     * @return timeout timeout
     */
    int getTimeout();

    /**
     * 初始化一个参与者.
     *
     * @param participant the participant
     */
    void init(XaParticipant participant);

    /**
     * 判断2个proxy是不是一致的.
     *
     * @param xaProxy the xa proxy
     * @return the boolean
     */
    boolean equals(RpcXaProxy xaProxy);

    /**
     * xa rpc 需要处理的命令.
     */
    enum XaCmd {
        /**
         * 初始化的时候进行处理.
         */
        START,

        /**
         * The Commit.
         *
         * @see RpcResource#commit(Xid, boolean)
         */
        COMMIT,

        /**
         * The Prepare.
         *
         * @see RpcResource#prepare(Xid)
         */
        PREPARE,

        /**
         * The Recover.
         *
         * @see RpcResource#recover(int)
         */
        RECOVER,

        /**
         * The Rollback.
         *
         * @see RpcResource#rollback(Xid)
         */
        ROLLBACK,
    }
}
