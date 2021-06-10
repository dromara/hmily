/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.core.context;

import lombok.Data;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;

import javax.transaction.xa.XAResource;

/**
 * HmilyTransactionContext.
 *
 * @author xiaoyu
 */
@Data
public class HmilyTransactionContext {

    /**
     * transId.
     */
    private Long transId;

    /**
     * participant id.
     */
    private Long participantId;

    /**
     * participant ref id.
     */
    private Long participantRefId;

    /**
     * this hmily action. {@linkplain HmilyActionEnum}
     */
    private int action;

    /**
     * 事务参与的角色. {@linkplain HmilyRoleEnum}
     */
    private int role;

    /**
     * transType.
     */
    private String transType;

    //以下为xa相关的参数.
    /**
     * xa相关的参数定义.
     */
    private XaParticipant xaParticipant;
}
