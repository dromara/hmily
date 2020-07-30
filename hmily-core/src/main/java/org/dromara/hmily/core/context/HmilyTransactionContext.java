/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.Serializable;
import lombok.Data;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.enums.HmilyRoleEnum;

/**
 * HmilyTransactionContext.
 *
 * @author xiaoyu
 */
@Data
public class HmilyTransactionContext implements Serializable {

    private static final long serialVersionUID = -5289080166922118073L;

    /**
     * transId.
     */
    private String transId;
    
    /**
     * participant id.
     */
    private String participantId;
    
    /**
     * participant ref id.
     */
    private String participantRefId;
    
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
    
}
