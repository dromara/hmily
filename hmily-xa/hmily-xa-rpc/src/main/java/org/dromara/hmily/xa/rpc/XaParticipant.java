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

import lombok.Data;
import org.dromara.hmily.core.context.HmilyTransactionContext;

import javax.transaction.xa.XAResource;

/**
 * XaParticipant .
 * 1、处理相关的参与者相关的数据Rpc的传传递.
 *
 * @author sixh chenbin
 */
@Data
public class XaParticipant {

    /**
     * 事务ID.
     */
    private String globalId;

    private String branchId;

    /**
     * @see XAResource
     */
    private int flag;

    private String cmd;
}
